package com.coremedia.labs.plugins.feedbackhub.imagga;

import com.coremedia.feedbackhub.adapter.FeedbackHubErrorCode;

/**
 * Error codes for Imagga adapter
 */
enum ImaggaFeedbackHubErrorCode implements FeedbackHubErrorCode {
  BASIC_AUTH_KEY_NOT_SET,
  UPLOAD_FAILED,
  LOGIN_ERROR,
  GET_TAGS_FROM_UPLOAD_FAILED,
  ERROR_PROCESSING_JSON,
  NOT_SUPPORTED_FILE_TYPE
}
