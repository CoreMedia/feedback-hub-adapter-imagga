import Config from "@jangaroo/runtime/Config";
import FeedbackHubImagga_properties from "./FeedbackHubImagga_properties";
import CopyResourceBundleProperties from "@coremedia/studio-client.main.editor-components/configuration/CopyResourceBundleProperties";
import StudioPlugin from "@coremedia/studio-client.main.editor-components/configuration/StudioPlugin";
import FeedbackHub_properties from "@coremedia/studio-client.main.feedback-hub-editor-components/FeedbackHub_properties";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import resourceManager from "@jangaroo/runtime/l10n/resourceManager";
interface ImaggaFeedbackHubStudioPluginConfig extends Config<StudioPlugin> {
}



    class ImaggaFeedbackHubStudioPlugin extends StudioPlugin{
  declare Config: ImaggaFeedbackHubStudioPluginConfig;

  static readonly xtype:string = "com.coremedia.labs.plugins.feedbackhub.imagga.config.imaggaFeedbackHubStudioPlugin";

  constructor(config:Config<ImaggaFeedbackHubStudioPlugin> = null){
    super( ConfigUtils.apply(Config(ImaggaFeedbackHubStudioPlugin, {

  configuration:[
    new CopyResourceBundleProperties({
            destination: resourceManager.getResourceBundle(null,FeedbackHub_properties),
            source: resourceManager.getResourceBundle(null,FeedbackHubImagga_properties)})
  ]


}),config));
  }}
export default ImaggaFeedbackHubStudioPlugin;
