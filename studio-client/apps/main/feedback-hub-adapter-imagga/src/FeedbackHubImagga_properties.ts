import CoreIcons_properties from "@coremedia/studio-client.core-icons/CoreIcons_properties";

/**
 * Interface values for ResourceBundle "FeedbackHubImagga".
 * @see FeedbackHubImagga_properties#INSTANCE
 */
interface FeedbackHubImagga_properties {

  imagga_iconCls: string;
  imagga_tooltip: string;
  imagga_title: string;
/**
 *Imagga Error Messages
 */
  imagga_error_BASIC_AUTH_KEY_NOT_SET: string;
  imagga_error_UPLOAD_FAILED: string;
  imagga_error_LOGIN_ERROR: string;
  imagga_error_GET_TAGS_FROM_UPLOAD_FAILED: string;
  imagga_error_ERROR_PROCESSING_JSON: string;
  imagga_error_NOT_SUPPORTED_FILE_TYPE: string;
}

/**
 * Singleton for the current user Locale's instance of ResourceBundle "FeedbackHubImagga".
 * @see FeedbackHubImagga_properties
 */
const FeedbackHubImagga_properties: FeedbackHubImagga_properties = {
  imagga_iconCls: CoreIcons_properties.imagga,
  imagga_tooltip: "Imagga Feedback",
  imagga_title: "Imagga",
  imagga_error_BASIC_AUTH_KEY_NOT_SET: "Please provide a valid basicAuthKey in the configuration for {0}.",
  imagga_error_UPLOAD_FAILED: "Upload of picture failed. Imagga responded with a problem: \"{1}\"",
  imagga_error_LOGIN_ERROR: "Login at Imagga failed. Please provide a valid credentials key in the configuration.",
  imagga_error_GET_TAGS_FROM_UPLOAD_FAILED: "Request for keywords to Imagga failed. Imagga responded with a problem: \"{1}\"",
  imagga_error_ERROR_PROCESSING_JSON: "Imagga is currently unavailable. Please try again later.",
  imagga_error_NOT_SUPPORTED_FILE_TYPE: "Please upload a valid file type. Imagga supports the file types \"JPG\" or \"PNG\".",
};

export default FeedbackHubImagga_properties;
