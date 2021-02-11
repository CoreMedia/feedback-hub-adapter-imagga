package com.coremedia.labs.plugins.feedbackhub.imagga;

import com.coremedia.cache.Cache;
import com.coremedia.feedbackhub.adapter.FeedbackHubAdapter;
import com.coremedia.feedbackhub.adapter.FeedbackHubAdapterFactory;
import com.coremedia.feedbackhub.adapter.FeedbackHubException;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Factory for {@link ImaggaFeedbackHubAdapter} instances
 */
@DefaultAnnotation(NonNull.class)
class ImaggaFeedbackHubAdapterFactory implements FeedbackHubAdapterFactory<ImaggaFeedbackHubAdapterSettings> {
  private static final String DEFAULT_URL = "https://api.imagga.com/v2";
  private static final int DEFAULT_MIN_ACCURACY = 30;
  // imagga default value of the "limit" url param, means "no limit"
  private static final int DEFAULT_LIMIT = -1;

  @Nullable
  private final Cache cache;

  ImaggaFeedbackHubAdapterFactory(@Nullable Cache cache) {
    this.cache = cache;
  }

  @Override
  public String getId() {
    return "imagga";
  }

  @Override
  public FeedbackHubAdapter create(ImaggaFeedbackHubAdapterSettings settings) {
    String basicAuthKey = settings.getBasicAuthKey();
    if (basicAuthKey == null || basicAuthKey.length() == 0) {
      throw new FeedbackHubException("settings must provide a basicAuthKey", ImaggaFeedbackHubErrorCode.BASIC_AUTH_KEY_NOT_SET);
    }
    String url = settings.getUrl();
    if (url == null) {
      url = DEFAULT_URL;
    }
    int minAccuracy = asInt(settings.getMinAccuracy(), DEFAULT_MIN_ACCURACY);
    int limit = asInt(settings.getLimit(), DEFAULT_LIMIT);
    return new ImaggaFeedbackHubAdapter(url, basicAuthKey, minAccuracy, limit, cache);
  }

  private static int asInt(Integer value, int dflt) {
    return value!=null ? value : dflt;
  }

  @Override
  public String toString() {
    return ImaggaFeedbackHubAdapterFactory.class.getName();
  }
}
