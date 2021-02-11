package com.coremedia.labs.plugins.feedbackhub.imagga;

import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cache.PeekResult;
import com.coremedia.cache.TimedDependency;
import com.coremedia.feedbackhub.adapter.Blob;
import com.coremedia.feedbackhub.adapter.FeedbackContext;
import com.coremedia.feedbackhub.adapter.FeedbackHubException;
import com.coremedia.feedbackhub.adapter.keywords.BlobKeywordsFeedbackHubAdapter;
import com.coremedia.feedbackhub.adapter.keywords.Keyword;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * The ImaggaFeedbackHubAdapter performs REST calls against the Imagga API.
 * First a given picture, or video is uploaded to Imagga, which returns an ID.
 * Then a second call is made with the received id, in order to get the keywords for the uploaded resource.
 * The keywords refer to a given locale. If no locale is passed to the Adapter, the keywords
 * refer to Imagga's default language, which is English ("en").
 */
class ImaggaFeedbackHubAdapter implements BlobKeywordsFeedbackHubAdapter {
  private static final Logger LOG = LoggerFactory.getLogger(ImaggaFeedbackHubAdapter.class);

  //URL fragments
  private static final String UPLOAD_URI = "/uploads";
  private static final String TAGS_URI = "/tags";

  //JSON keys
  private static final String UPLOAD_ID_KEY = "upload_id";
  private static final String RESULT_KEY = "result";
  private static final String TAGS_KEY = "tags";
  private static final String CONFIDENCE_KEY = "confidence";
  private static final String TAG_KEY = "tag";

  private static final String EN_DEFAULT_LANGUAGE = "en";
  private static final String STATUS_KEY = "status";
  private static final String TEXT_KEY = "text";
  private static final String BOUNDARY = "ImageUpload";
  private static final String PNG = "png";
  private static final String JPG = "jpg";

  private final Cache cache;
  private final String url;
  private final String basicAuthKey;
  private final int minAccuracy;
  private final int limit;

  private HttpClient client;
  private final JsonParser jsonParser;

  ImaggaFeedbackHubAdapter(@NonNull String url,
                           @NonNull String basicAuthKey,
                           int minAccuracy,
                           int limit,
                           @Nullable Cache cache) {
    this.url = url;
    this.basicAuthKey = basicAuthKey;
    this.minAccuracy = minAccuracy;
    this.limit = limit;
    this.cache = cache;

    client = HttpClient.newHttpClient();
    jsonParser = new JsonParser();

    if (cache == null) {
      LOG.info("No cache in {}.  This works functionally, but is too slow for production use.", getClass().getName());
    }
  }


  // --- BlobKeywordsFeedbackHubAdapter -----------------------------

  @Override
  @NonNull
  public CompletionStage<List<Keyword>> getKeywords(FeedbackContext context, Blob blob, @Nullable Locale locale) {
    try {
      return upload(blob).thenCompose(uploadId -> tags(uploadId, locale, blob));
    } catch (FeedbackHubException e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ImaggaFeedbackHubAdapter.class.getSimpleName() + "[", "]")
            .add("minAccuracy=" + minAccuracy)
            .add("limit=" + limit)
            .toString();
  }


  // --- internal ---------------------------------------------------

  private CompletionStage<String> upload(Blob blob) {
    CompletionStage<String> result = peekAsFuture(new UploadIdCacheKey(blob, url, basicAuthKey));
    return result != null ? result : uploadUncached(blob);
  }

  private CompletionStage<String> uploadUncached(Blob blob) {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url + UPLOAD_URI))
            .header("Authorization", "Basic " + basicAuthKey)
            .header("Content-Type", "multipart/form-data;boundary=" + BOUNDARY)
            .POST(HttpRequest.BodyPublishers.ofInputStream(getInputStreamSupplierForBlob(blob)))
            .build();
    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(httpResponse -> extractUploadId(httpResponse, blob));
  }

  private String extractUploadId(HttpResponse<String> uploadResponse, Blob blob) {
    if (uploadResponse.statusCode() == 200) {
      // Imagga upload IDs are valid for 24h. Cache them slightly shorter
      // in order to prevent follow-up round trips with outdated IDs.
      UploadIdCacheKey cacheKey = new UploadIdCacheKey(blob, url, basicAuthKey);
      return cached(cacheKey, getUploadIdFromResponse(uploadResponse), 23, TimeUnit.HOURS, cacheKey.asDependency());
    } else {
      ImaggaFeedbackHubErrorCode errorCode;
      List<String> arguments = null;
      if(uploadResponse.statusCode() == 401){
        errorCode = ImaggaFeedbackHubErrorCode.LOGIN_ERROR;
      } else {
        arguments = Collections.singletonList(getErrorMessageFromResponse(uploadResponse));
        errorCode = ImaggaFeedbackHubErrorCode.UPLOAD_FAILED;
      }
      throw new FeedbackHubException("The upload of the blob with eTag '" + blob.getETag() + "' " +
              "failed with status Code '" + uploadResponse.statusCode() + "' and message:'" + uploadResponse.body() + "'", errorCode, arguments);
    }
  }

  private CompletionStage<List<Keyword>> tags(String uploadId, @Nullable Locale locale, Blob blob) {
    CompletionStage<List<Keyword>> result = peekAsFuture(new TagsCacheKey(uploadId, locale, limit, minAccuracy));
    return result != null ? result : tagsUncached(uploadId, locale, blob);
  }

  private CompletableFuture<List<Keyword>> tagsUncached(String uploadId, @Nullable Locale locale, Blob blob) {
    String idP = "image_upload_id=" + uploadId;
    String languageP = locale != null ? "language=" + locale.getLanguage() : null;
    String limitP = "limit=" + limit;
    String thresholdP = "threshold=" + minAccuracy + ".0";
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url + TAGS_URI + concatUrlParameters(idP, languageP, limitP, thresholdP)))
            .header("Authorization", "Basic " + basicAuthKey)
            .GET()
            .build();
    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(httpResponse -> extractKeywords(httpResponse, uploadId, locale, blob));
  }

  private static String concatUrlParameters(String... args) {
    String params = Stream.of(args).filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining("&"));
    return params.isEmpty() ? "" : "?" + params;
  }

  private List<Keyword> extractKeywords(HttpResponse<String> tagsResponse, String uploadId, @Nullable Locale locale, Blob blob) {
    if (tagsResponse.statusCode() == 200) {
      List<Keyword> keywords = getKeywordsFromResponse(locale, tagsResponse);
      // Cache a little longer than the uploadId, to prevent a second round trip with the same uploadId.
      return cached(new TagsCacheKey(uploadId, locale, limit, minAccuracy), keywords, 23 * 60 + 1L, TimeUnit.MINUTES, null);
    } else {
      // Robustness against Imagga: Sometimes they cannot handle their own
      // uploadIds. (E.g. the tags request fails with code 400, which we
      // observed during development.)  Invalidate the uploadId in such cases,
      // so that an editor's retry starts over with a complete new roundtrip.
      if (cache != null) {
        cache.invalidate(new UploadIdCacheKey(blob, url, basicAuthKey).asDependency());
      }
      ImaggaFeedbackHubErrorCode errorCode;
      List<String> arguments = null;
      if(tagsResponse.statusCode() == 401){
        errorCode = ImaggaFeedbackHubErrorCode.LOGIN_ERROR;
      } else {
        arguments = Collections.singletonList(getErrorMessageFromResponse(tagsResponse));
        errorCode = ImaggaFeedbackHubErrorCode.GET_TAGS_FROM_UPLOAD_FAILED;
      }
      throw new FeedbackHubException("The request for tags with uploadId '" + uploadId + "' " +
              "failed with status Code '" + tagsResponse.statusCode() + "' and message:'" + tagsResponse.body() + "'", errorCode, arguments);
    }
  }


  private String getLanguage(@Nullable Locale locale) {
    String language;
    if (locale != null) {
      language = locale.getLanguage();
    } else {
      //if no language is set, the imagga service will return "en" as default
      language = EN_DEFAULT_LANGUAGE;
    }
    return language;
  }

  private String getErrorMessageFromResponse(HttpResponse<String> response) {
    try {
      String body = response.body();
      String imaggaErrorMessage = "";
      if (body != null && !body.isEmpty()) {
        JsonElement responseBody = jsonParser.parse(body);
        JsonObject status = responseBody.getAsJsonObject().getAsJsonObject(STATUS_KEY);
        if (status != null) {
          JsonElement text = status.get(TEXT_KEY);
          imaggaErrorMessage = text.getAsString();
        }
      }
      return imaggaErrorMessage;
    } catch (Exception e) {
      throw new FeedbackHubException("There was an exception while processing the response: '" + response.body() + "'", ImaggaFeedbackHubErrorCode.ERROR_PROCESSING_JSON, null);
    }
  }

  private String getUploadIdFromResponse(HttpResponse<String> response) {
    try {
      JsonObject responseJson = jsonParser.parse(response.body()).getAsJsonObject();
      JsonObject result = responseJson.getAsJsonObject(RESULT_KEY);
      return result.get(UPLOAD_ID_KEY).getAsString();
    } catch (Exception e) {
      throw new FeedbackHubException("There was an exception while processing the response: '" + response.body() + "'", ImaggaFeedbackHubErrorCode.ERROR_PROCESSING_JSON, null);
    }
  }

  // impl note: getKeywordsFromResponse must not filter the result,
  // because we pose a limited query, and post filtering limited queries
  // corrupts the result.  If you need filtering, remove the limit parameter
  // from the request and apply the limit here, after the filtering.
  private List<Keyword> getKeywordsFromResponse(@Nullable Locale locale, HttpResponse<String> response) {
    try {
      List<Keyword> resultKeyWords = new ArrayList<>();
      JsonElement responseBody = jsonParser.parse(response.body());
      JsonObject responseJson = responseBody.getAsJsonObject();
      JsonObject result = responseJson.getAsJsonObject(RESULT_KEY);
      result.getAsJsonArray(TAGS_KEY).forEach(tag -> {
        JsonObject tagObject = tag.getAsJsonObject();
        double confidence = tagObject.get(CONFIDENCE_KEY).getAsDouble();
        JsonObject tagObjectAsJsonObject = tagObject.getAsJsonObject(TAG_KEY);
        String tagValue = tagObjectAsJsonObject.get(getLanguage(locale)).getAsString();
        resultKeyWords.add(new Keyword(tagValue, confidence));
      });
      return resultKeyWords;
    } catch (Exception e) {
      throw new FeedbackHubException("There was an exception while processing the response: '" + response.body() + "'", e, ImaggaFeedbackHubErrorCode.ERROR_PROCESSING_JSON, null);
    }
  }

  /**
   * A multipart request should have the following structure:
   *
   ** Content-Type: multipart/form-data; boundary=ImageUpload
   *
   *+ --ImageUpload
   ** Content-Disposition: form-data; name="image"
   ** <…file content…>
   *
   *  ImageUpload--
   */
  private Supplier<InputStream> getInputStreamSupplierForBlob(Blob blob) {
    String crlf = "\r\n";
    String twoHyphens = "--";

    String prefix = (twoHyphens + BOUNDARY + crlf);
    String blobName = blob.getETag();
    Optional<String> extension = blob.findExtension();
    if (extension.isPresent()) {
      String fileExtension = extension.get();
      if(!fileExtension.equals(PNG) && !fileExtension.equals(JPG)){
        throw new FeedbackHubException("Unsupported file format. Only .png and .jpg are allowed", ImaggaFeedbackHubErrorCode.NOT_SUPPORTED_FILE_TYPE, Collections.singletonList(fileExtension));
      }
      blobName = blobName + "." + extension;
    }
    String parameters = ("Content-Disposition: form-data; name=\"image\";filename=\"" + blobName + "\"" + crlf);
    String pictureContext = prefix + parameters + crlf;

    ByteArrayInputStream pictureContextStream = new ByteArrayInputStream(pictureContext.getBytes());
    ByteArrayInputStream endingForMultiPartStream = new ByteArrayInputStream((crlf + crlf + BOUNDARY + twoHyphens).getBytes());

    SequenceInputStream sequenceInputStream = new SequenceInputStream(pictureContextStream, blob.getInputStream());
    SequenceInputStream sequenceInputStreamWithEnding = new SequenceInputStream(sequenceInputStream, endingForMultiPartStream);

    return () -> sequenceInputStreamWithEnding;
  }

  @Nullable
  private <T> CompletableFuture<T> peekAsFuture(CacheKey<T> cacheKey) {
    if (cache != null) {
      PeekResult<T> peek = cache.peek(cacheKey);
      if (peek.isPresent()) {
        return CompletableFuture.completedFuture(peek.get());
      }
    }
    return null;
  }

  private <T> T cached(CacheKey<T> cacheKey, T value, long duration, TimeUnit timeUnit, Object invalidationDependency) {
    if (cache != null) {
      TimedDependency timedDependency = cache.createRelativeTimedDependency(timeUnit.toMillis(duration));
      Set<Object> dependencies = Stream.of(timedDependency, invalidationDependency)
              .filter(Objects::nonNull)
              .collect(Collectors.toUnmodifiableSet());
      cache.inject(cache.getCurrentCacheTime(), cacheKey, value, dependencies);
    }
    return value;
  }


  // --- cache keys -------------------------------------------------

  private static class UploadIdCacheKey extends CacheKey<String> {
    private final String eTag;

    // Include url and authKey in the cache key, otherwise you could not
    // check configuration changes immediately.  During regular productive
    // Studio operation, these will be effectively constant, though.
    private final String url;
    private final String authKey;

    UploadIdCacheKey(@NonNull Blob blob, @NonNull String url, @NonNull String authKey) {
      eTag = requireNonNull(blob.getETag(), "blob must have an etag");
      this.authKey = authKey;
      this.url = url;
    }

    @Override
    public String evaluate(Cache cache) {
      throw new UnsupportedOperationException("My values can only be injected and peeked.");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      UploadIdCacheKey that = (UploadIdCacheKey) o;
      return eTag.equals(that.eTag) &&
              url.equals(that.url) &&
              authKey.equals(that.authKey);
    }

    @Override
    public int hashCode() {
      return Objects.hash(eTag, url, authKey);
    }

    String asDependency() {
      return eTag + ", " + url + ", " + authKey;
    }
  }

  private static class TagsCacheKey extends CacheKey<List<Keyword>> {
    private final String uploadId;
    private final Locale locale;
    private final int minAccuracy;
    private final int limit;

    TagsCacheKey(String uploadId, @Nullable Locale locale, int minAccuracy, int limit) {
      this.uploadId = requireNonNull(uploadId);
      this.locale = locale;
      this.minAccuracy = minAccuracy;
      this.limit = limit;
    }

    @Override
    public List<Keyword> evaluate(Cache cache) {
      throw new UnsupportedOperationException("My values can only be injected and peeked.");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TagsCacheKey that = (TagsCacheKey) o;
      return minAccuracy == that.minAccuracy &&
              limit == that.limit &&
              uploadId.equals(that.uploadId) &&
              Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
      return Objects.hash(uploadId, locale, minAccuracy, limit);
    }
  }

  @VisibleForTesting
  void setClient(HttpClient client) {
    this.client = client;
  }
}
