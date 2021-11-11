/** @type { import('@jangaroo/core').IJangarooConfig } */
module.exports = {
  type: "code",
  extName: "com.coremedia.labs.plugins__studio-client.feedback-hub-adapter-imagga",
  extNamespace: "com.coremedia.labs.plugins.feedbackhub.imagga",
  sencha: {
    studioPlugins: [
      {
        mainClass: "com.coremedia.labs.plugins.feedbackhub.imagga.ImaggaFeedbackHubStudioPlugin",
        name: "FeedbackHub for Imagga",
      },
    ],
  },
  command: {
    build: {
      ignoreTypeErrors: true
    },
  },
};
