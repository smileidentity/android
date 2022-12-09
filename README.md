# Smile Identity Android SDK

## Overview
This repo encompasses everything necessary for the Smile Identity Android SDK. 
It is a multi-module project consisting of the following modules:
- [`public/networking`](https://github.com/smileidentity/android/tree/main/public/networking) - handles all network requests to the API
- [`public/sample`](https://github.com/smileidentity/android/tree/main/public/sample) - a sample app that demonstrates the SDK (both UI and networking)
- [`ui`](https://github.com/smileidentity/android/tree/main/ui) - all UI and CV components

## Setup
- Android Studio 2021.3.1 or higher
- Android Gradle Plugin 7.3.1 or higher
- Android SDK 33
- Local JDK 11 installation (potentially up to JDK 18, but not tested)
- Add `SENTRY_DSN=<sentry dsn value from Sentry Portal>` to `~/gradle/gradle.properties`

## Building
- `./gradlew build` - builds, lints, and tests all modules

## Linting and Formatting
We use [ktlint](https://github.com/pinterest/ktlint) via 
[this plugin](https://github.com/jlleitschuh/ktlint-gradle) to enforce Kotlin code style.

To run the linter, run `./gradlew ktlintCheck`. To run the formatter, run `./gradlew ktlintFormat`

The codeStyles found in `.idea/codeStyles` are used by Android Studio to enforce the code style
(taken from https://pinterest.github.io/ktlint/rules/configuration-intellij-idea/)

