![CoreMedia Labs Logo](https://documentation.coremedia.com/badges/banner_coremedia_labs_wide.png "CoreMedia Labs Logo")

![CoreMedia Content Cloud Version](https://img.shields.io/static/v1?message=2104&label=CoreMedia%20Content%20Cloud&style=for-the-badge&labelColor=666666&color=672779 
"This badge shows the CoreMedia version this project is compatible with. 
Please read the versioning section of the project to see what other CoreMedia versions are supported and how to find them."
)
![Status](https://img.shields.io/static/v1?message=active&label=Status&style=for-the-badge&labelColor=666666&color=2FAC66 
"The status badge describes if the project is maintained. Possible values are active and inactive. 
If a project is inactive it means that the development has been discontinued and won't support future CoreMedia versions."
)


# Imagga Adapter for Feedback Hub 

## Overview 

This plugin implements the Feedback Hub's API to connect CoreMedia to the external 
systems `Imagga`, in order to provide keywords for selected content.

## Versioning

To find out which CoreMedia versions are supported by this project, 
please take look at the releases section or on the existing branches. 
To find the matching version of your CoreMedia system, please checkout the branch 
with the corresponding name. For example, 
if your CoreMedia version is 2104.1, checkout the branch 2104.1.

## Project Setup

## Plugin Configuration

To activate this plugin, you need to follow the documentation steps, described in the `Studio Development Manual`, under the section `Feedback Hub`. 

Assuming you have read the Feedback Hubs documentation and are familiar with the terminology of CoreMedia Feedback Hub, the next steps will shortly explain how to activate the `Imagga Adapter`:
 
- provide a `settings document` to configure your `Imagga Adapter` as described in the documentation
- within your `Imagga` settings document you need to provide the value `imagga` for the key `factoryId` (The value must match the value, returned by the `ImaggaFeedbackHubAdapterFactory#getId` method)
- within your `Imagga` settings document you need to provide the following String values underneath the struct `settings`: 
    - `sourceBlobProperty`: the name of the blob property that stores a picture, that should be evaluated by Imagga
    - `basicAuthKey` : the authentication key, provided by Imagga after creating an account that allows using the API

## CoreMedia Labs

Welcome to [CoreMedia Labs](https://blog.coremedia.com/labs/)! This repository
is part of a platform for developers who want to have a look under the hood or
get some hands-on understanding of the vast and compelling capabilities of
CoreMedia. Whatever your experience level with CoreMedia is, we've got something
for you.

Each project in our Labs platform is an extra feature to be used with CoreMedia,
including extensions, tools and 3rd party integrations. We provide some test
data and explanatory videos for non-customers and for insiders there is
open-source code and instructions on integrating the feature into your
CoreMedia workspace. 

The code we provide is meant to be example code, illustrating a set of features
that could be used to enhance your CoreMedia experience. We'd love to hear your
feedback on use-cases and further developments! If you're having problems with
our code, please refer to our issues section. If you already have a solution to 
an issue, we love to review and integrate your pull requests. 

