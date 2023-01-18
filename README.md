# Smile Identity Android SDK

## Overview

This repo encompasses everything necessary for the Smile Identity Android SDK. It is a multi-module
project consisting of the following modules:

- [`public/networking`](https://github.com/smileidentity/android/tree/main/public/networking) -
  handles all network requests to the API
- [`public/sample`](https://github.com/smileidentity/android/tree/main/public/sample) - a sample app
  that demonstrates the SDK (both UI and networking)
- [`ui`](https://github.com/smileidentity/android/tree/main/ui) - all UI and CV components

## Setup

- Android Studio 2021.3.1 or higher
- Android Gradle Plugin 7.3.1 or higher
- Android SDK 33
- Local JDK 11 installation (potentially up to JDK 18, but not tested)
- Add `SENTRY_DSN=<sentry dsn value from Sentry Portal>` to `~/.gradle/gradle.properties`

## Building

- `./gradlew build` - builds, lints, and tests all modules

## Linting and Formatting

We use [ktlint](https://github.com/pinterest/ktlint) via
[this plugin](https://github.com/jlleitschuh/ktlint-gradle) to enforce Kotlin code style.

To run the linter, run `./gradlew ktlintCheck`. To run the formatter, run `./gradlew ktlintFormat`

The codeStyles found in `.idea/codeStyles` are used by Android Studio to enforce the code style
(taken from https://pinterest.github.io/ktlint/rules/configuration-intellij-idea/)

## FAQs

- Q: Why are source sets are located in `$module/src/main/java`, even though we're using Kotlin?
    - A: To simplify configuration. `$module/src/main/kotlin` would require custom build setup. And
      at the end of the day, it doesn't really matter. Kotlin files can go in `java` just fine
- Q: What is the `public` top level directory?
    - A: This indicates which components are open source. Currently they reside in the same repo,
      but will be moved to a separate repo in the future, and included here as a git submodule
      instead
- Q: Why can't I see coverage info inside Android Studio?
    - A: https://issuetracker.google.com/issues/241258667
- Q: How come I can't preview Markdown files within Android Studio?
    - A: You need to change the JetBrains Runtime to the latest version **with JCEF**. see
      also: https://stackoverflow.com/a/71063742
