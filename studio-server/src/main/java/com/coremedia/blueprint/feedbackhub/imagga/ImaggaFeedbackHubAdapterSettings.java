package com.coremedia.blueprint.feedbackhub.imagga;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * ImaggaFeedbackHubAdapterSettings declares the properties for the
 * {@link ImaggaFeedbackHubAdapter}.
 * <p>
 * Impl note: Instances are generated automatically as proxies, backed by
 * settings.  Therefore, all methods are nullable.
 */
interface ImaggaFeedbackHubAdapterSettings {
  /**
   * The Imagga URL to connect to.
   * <p>
   * The default URL is https://api.imagga.com/v2 .
   */
  @Nullable
  String getUrl();

  /**
   * The authentication key for Imagga.
   * <p>
   * While this method is nullable for technical reasons, this property is
   * mandatory, and the ImaggaFeedbackHubAdapter does not work without it.
   */
  @Nullable
  String getBasicAuthKey();

  /**
   * The minimum accuracy for a tag to be included in the result.
   * <p>
   * The value must be a percentage value between 0 and 100.
   * The higher the value, the fewer tags are accepted.
   * Default is 30.
   */
  @Nullable
  Integer getMinAccuracy();

  /**
   * The maximum number of tags to be included in the result.
   * <p>
   * The value must be a positive integer, or null for unlimited results.
   */
  @Nullable
  Integer getLimit();
}
