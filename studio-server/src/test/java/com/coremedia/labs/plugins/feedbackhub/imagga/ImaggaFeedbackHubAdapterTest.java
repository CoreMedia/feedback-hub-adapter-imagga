package com.coremedia.labs.plugins.feedbackhub.imagga;

import com.coremedia.feedbackhub.adapter.Blob;
import com.coremedia.feedbackhub.adapter.FeedbackContext;
import com.coremedia.feedbackhub.adapter.FeedbackHubException;
import com.coremedia.feedbackhub.adapter.keywords.Keyword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ImaggaFeedbackHubAdapterTest {

  private static final String UPLOAD_ID = "12345";
  private static final String URL = "http://test";
  private static final String BASE_AUTH_KEY = "key";
  private static final int MIN_ACCURACY = 10;
  private static final int LIMIT = 10;
  private static final String ETAG = "etag";
  private static final String ERROR_TEXT = "errorText";


  private ImaggaFeedbackHubAdapter imaggaFeedbackHubAdapter;


  @Mock
  Blob blob;

  @Mock
  HttpClient httpClient;

  @Mock
  HttpResponse uploadIDResponse;

  @Mock
  HttpResponse keyWordsResponse;

  private Locale locale;

  @Mock
  FeedbackContext context;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    locale = new Locale("de");
    imaggaFeedbackHubAdapter = new ImaggaFeedbackHubAdapter(URL, BASE_AUTH_KEY, MIN_ACCURACY, LIMIT, null);
    imaggaFeedbackHubAdapter.setClient(httpClient);

    //blob
    when(blob.getETag()).thenReturn(ETAG);
  }


  @Test
  void getKeywords() throws ExecutionException, InterruptedException {
    mockUploadRequest(true, false, false);
    mockKeyWordsRequest(true, false, false);

    CompletionStage<List<Keyword>> keyWordsStage = imaggaFeedbackHubAdapter.getKeywords(context, blob, locale);
    List<Keyword> keywords = keyWordsStage.toCompletableFuture().get();
    Keyword keyword = new Keyword("keyword", 10.0);
    assertThat(keywords).containsExactly(keyword);
  }

  @Test
  void upLoadFailed() throws InterruptedException {
    mockUploadRequest(false, false, false);
    CompletionStage<List<Keyword>> keyWordsStage = imaggaFeedbackHubAdapter.getKeywords(context, blob, locale);

    ImaggaFeedbackHubErrorCode errorCode = null;
    List<String> arguments = null;
    try {
      keyWordsStage.toCompletableFuture().get();
    } catch (ExecutionException e) {
      FeedbackHubException cause = (FeedbackHubException) e.getCause();
      errorCode = (ImaggaFeedbackHubErrorCode) cause.getErrorCode();
      arguments = cause.getArguments();
    }
    assertThat(errorCode).isEqualTo(ImaggaFeedbackHubErrorCode.UPLOAD_FAILED);
    assertThat(arguments).containsExactly(ERROR_TEXT);
  }

  @Test
  void loginUploadFailed() throws InterruptedException {
    mockUploadRequest(false, false, true);
    CompletionStage<List<Keyword>> keyWordsStage = imaggaFeedbackHubAdapter.getKeywords(context, blob, locale);

    ImaggaFeedbackHubErrorCode errorCode = null;
    try {
      keyWordsStage.toCompletableFuture().get();
    } catch (ExecutionException e) {
      FeedbackHubException cause = (FeedbackHubException) e.getCause();
      errorCode = (ImaggaFeedbackHubErrorCode) cause.getErrorCode();
    }
    assertThat(errorCode).isEqualTo(ImaggaFeedbackHubErrorCode.LOGIN_ERROR);
  }


  @Test
  void getKeyWordsFailed() throws InterruptedException {
    mockUploadRequest(true, false, false);
    mockKeyWordsRequest(false, false, false);
    CompletionStage<List<Keyword>> keyWordsStage = imaggaFeedbackHubAdapter.getKeywords(context, blob, locale);

    ImaggaFeedbackHubErrorCode errorCode = null;
    List<String> arguments = null;
    try {
      keyWordsStage.toCompletableFuture().get();
    } catch (ExecutionException e) {
      FeedbackHubException cause = (FeedbackHubException) e.getCause();
      errorCode = (ImaggaFeedbackHubErrorCode) cause.getErrorCode();
      arguments = cause.getArguments();
    }
    assertThat(errorCode).isEqualTo(ImaggaFeedbackHubErrorCode.GET_TAGS_FROM_UPLOAD_FAILED);
    assertThat(arguments).containsExactly(ERROR_TEXT);
  }

  @Test
  void loginGetKeyWordsFailed() throws InterruptedException {
    mockUploadRequest(true, false, false);
    mockKeyWordsRequest(false, false, true);
    CompletionStage<List<Keyword>> keyWordsStage = imaggaFeedbackHubAdapter.getKeywords(context, blob, locale);

    ImaggaFeedbackHubErrorCode errorCode = null;
    try {
      keyWordsStage.toCompletableFuture().get();
    } catch (ExecutionException e) {
      FeedbackHubException cause = (FeedbackHubException) e.getCause();
      errorCode = (ImaggaFeedbackHubErrorCode) cause.getErrorCode();
    }
    assertThat(errorCode).isEqualTo(ImaggaFeedbackHubErrorCode.LOGIN_ERROR);
  }

  @Test
  void getIdFromUploadResponseFailed() throws InterruptedException {
    mockUploadRequest(true, true, false);
    CompletionStage<List<Keyword>> keyWordsStage = imaggaFeedbackHubAdapter.getKeywords(context, blob, locale);

    ImaggaFeedbackHubErrorCode errorCode = null;
    try {
      keyWordsStage.toCompletableFuture().get();
    } catch (ExecutionException e) {
      FeedbackHubException cause = (FeedbackHubException) e.getCause();
      errorCode = (ImaggaFeedbackHubErrorCode) cause.getErrorCode();
    }
    assertThat(errorCode).isEqualTo(ImaggaFeedbackHubErrorCode.ERROR_PROCESSING_JSON);
  }

  @Test
  void getKeyWordsFromKeywordsResponseFailed() throws InterruptedException {
    mockUploadRequest(true, false, false);
    mockKeyWordsRequest(true, true, false);
    CompletionStage<List<Keyword>> keyWordsStage = imaggaFeedbackHubAdapter.getKeywords(context, blob, locale);

    ImaggaFeedbackHubErrorCode errorCode = null;
    try {
      keyWordsStage.toCompletableFuture().get();
    } catch (ExecutionException e) {
      FeedbackHubException cause = (FeedbackHubException) e.getCause();
      errorCode = (ImaggaFeedbackHubErrorCode) cause.getErrorCode();
    }
    assertThat(errorCode).isEqualTo(ImaggaFeedbackHubErrorCode.ERROR_PROCESSING_JSON);
  }


  private void mockKeyWordsRequest(boolean success, boolean responseJsonMalformed, boolean loginFailed) {
    String idP = "image_upload_id=" + UPLOAD_ID;
    String languageP = locale != null ? "language=" + locale.getLanguage() : null;
    String limitP = "limit=" + LIMIT;
    String thresholdP = "threshold=" + MIN_ACCURACY + ".0";
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL + "/tags" + concatUrlParameters(idP, languageP, limitP, thresholdP)))
            .header("Authorization", "Basic " + BASE_AUTH_KEY)
            .GET()
            .build();

    CompletableFuture<HttpResponse<Object>> keyWordResponseFuture = CompletableFuture.completedFuture(keyWordsResponse);
    when(httpClient.sendAsync(eq(request), any())).thenReturn(keyWordResponseFuture);
    if (success) {
      when(keyWordsResponse.statusCode()).thenReturn(200);
      if (responseJsonMalformed) {
        when(keyWordsResponse.body()).thenReturn("malformed");
      } else {
        when(keyWordsResponse.body()).thenReturn("{result:{tags:[{confidence:10,tag:{de:keyword}}]}}");
      }
    } else {
      if(loginFailed){
        when(keyWordsResponse.statusCode()).thenReturn(401);
      } else {
        when(keyWordsResponse.statusCode()).thenReturn(400);
        when(keyWordsResponse.body()).thenReturn("{status : {text:" + ERROR_TEXT + "}}");
      }
    }
  }

  private void mockUploadRequest(boolean success, boolean responseJsonMalformed, boolean loginFailed) {
    //upload request
    CompletableFuture<HttpResponse<Object>> uploadIDResponseFuture = CompletableFuture.completedFuture(uploadIDResponse);
    when(httpClient.sendAsync(any(), any())).thenReturn(uploadIDResponseFuture);

    if (success) {
      when(uploadIDResponse.statusCode()).thenReturn(200);
      if (responseJsonMalformed) {
        when(uploadIDResponse.body()).thenReturn("malformed");
      } else {
        when(uploadIDResponse.body()).thenReturn("{result:{upload_id:" + UPLOAD_ID + "}}");
      }
    } else {
      if(loginFailed){
        when(uploadIDResponse.statusCode()).thenReturn(401);
      }else {
        when(uploadIDResponse.statusCode()).thenReturn(400);
        when(uploadIDResponse.body()).thenReturn("{status : {text:" + ERROR_TEXT + "}}");
      }
    }

  }

  private static String concatUrlParameters(String... args) {
    String params = Stream.of(args).filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining("&"));
    return params.isEmpty() ? "" : "?" + params;
  }
}
