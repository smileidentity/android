# Smile ID Android SDK

[![Build](https://github.com/smileidentity/android/actions/workflows/build.yaml/badge.svg)](https://github.com/smileidentity/android/actions/workflows/build.yaml)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.smileidentity/android-sdk?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/com/smileidentity/android-sdk/)
[![Maven Central](https://img.shields.io/maven-central/v/com.smileidentity/android-sdk)](https://mvnrepository.com/artifact/com.smileidentity/android-sdk)
[![Build Sample App](https://github.com/smileidentity/android/actions/workflows/build_app.yaml/badge.svg)](https://github.com/smileidentity/android/actions/workflows/build_app.yaml)

Smile Identity provides the best solutions for real time Digital KYC, identity verification, user
onboarding, and user authentication across Africa.

If you haven’t already, 
[sign up for a free Smile Identity account](https://www.smileidentity.com/schedule-a-demo/), which 
comes with Sandbox access.

Please see [CHANGELOG.md](CHANGELOG.md) or 
[Releases](https://github.com/smileidentity/android/releases) for the most recent version and 
release notes


## Getting Started

Full documentation is available at https://docs.smileidentity.com/integration-options/mobile

Javadocs are available at https://javadoc.io/doc/com.smileidentity/android-sdk/latest/index.html

The [sample app](sample/src/main/java/com/smileidentity/sample/compose/MainScreen.kt) included in 
this repo is a good reference implementation

#### 0. Requirements

- Android 5.0 (API level 21) and above
- Google Play Services

#### 1. Dependency

The SDK is available on Maven Central. To use it, add the following to your `build.gradle`:

```groovy
implementation("com.smileidentity:android-sdk:<latest-version>")
```

#### 2. Smile Config

Please download your `smile_config.json` file from the 
[Smile ID Portal](https://portal.smileidentity.com/sdk) and add it to your `assets` directory (e.g. 
`app/src/main/assets`). You may need to create the directory if it does not already exist. 

#### 3. Initialization

The SDK should be initialized within your `Application` class' `onCreate`:

```kotlin
SmileID.initialize(this) 
```

## UI Components

All UI functionality is exposed via either Jetpack Compose or Fragments

#### Jetpack Compose

All Composables are available under the `SmileID` object. 

e.g.
```kotlin
SmileID.SmartSelfieEnrollment()
SmileID.SmartSelfieAuthentication()
SmileID.DocumentVerification()
SmileID.BiometricKYC()
```

#### Fragment

All Fragments are available under the `com.smileidentity.fragment` package.

#### Theming

To customize the theme, you can pass in a custom `ColorScheme` to the SmileID composable, OR
override [the color resources defined in the SDK](lib/src/main/res/values/colors.xml)

## API

To make raw API requests, you can use `SmileID.api` (requires coroutines)

## Docs

Please visit https://docs.smileidentity.com/integration-options/mobile for full documentation

## Contributing

Bug reports and Pull Requests are welcomed. Please see [CONTRIBUTING.md](CONTRIBUTING.md)

## License

[MIT License](LICENSE)
