const { jangarooConfig } = require("@jangaroo/core");

module.exports = jangarooConfig({
  type: "code",
  sencha: {
    name: "com.coremedia.labs.plugins__studio-client.feedback-hub-adapter-imagga",
    namespace: "com.coremedia.labs.plugins.feedbackhub.imagga",
    studioPlugins: [
      {
        mainClass: "com.coremedia.labs.plugins.feedbackhub.imagga.ImaggaFeedbackHubStudioPlugin",
        name: "FeedbackHub for Imagga",
      },
    ],
  },
});
