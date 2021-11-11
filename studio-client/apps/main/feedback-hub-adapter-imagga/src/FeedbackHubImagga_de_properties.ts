import ResourceBundleUtil from "@jangaroo/runtime/l10n/ResourceBundleUtil";
import FeedbackHubImagga_properties from "./FeedbackHubImagga_properties";

/**
 * Overrides of ResourceBundle "FeedbackHubImagga" for Locale "de".
 * @see FeedbackHubImagga_properties#INSTANCE
 */
ResourceBundleUtil.override(FeedbackHubImagga_properties, {
  imagga_error_BASIC_AUTH_KEY_NOT_SET: "Bitte geben Sie einen gültigen basicAuthKey in der Konfiguration für {0} an.",
  imagga_error_UPLOAD_FAILED: "Das Bild konnte nicht hochgeladen werden. Imagga hat einen Fehler gemeldet: \"{1}\"",
  imagga_error_LOGIN_ERROR: "Login bei Imagga fehlgeschlagen. Bitte geben Sie einen gültigen credentials key in der Konfiguration an.",
  imagga_error_GET_TAGS_FROM_UPLOAD_FAILED: "Tags konnten nicht geladen werden. Imagga hat einen Fehler gemeldet: \"{1}\"",
  imagga_error_ERROR_PROCESSING_JSON: "Imagga ist derzeit nicht verfügbar. Versuchen sie es später noch einmal.",
  imagga_error_NOT_SUPPORTED_FILE_TYPE: "Bitte laden Sie einen gültigen Dateitypen hoch. Imagga unterstützt die Dateitypen \"jpg\" oder \"png\".",
});
