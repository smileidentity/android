# Smile Identity Android SDK

## Setup
- Add `SENTRY_DSN=` to `~/gradle/gradle.properties`

## Overview
This repo encompasses everything necessary for the Smile Identity Android SDK. 
It is a multi-module project consisting of the following modules:
- `public/networking` - handles all network requests to the API
- `ui` - handles all UI and CV components
- `public/sample` - a sample app that demonstrates the SDK (both ui and networking)

## Building
- `./gradlew build` - builds, lints, and tests all modules

## Linting and Formatting
We use [ktlint](https://github.com/pinterest/ktlint) via 
[this plugin](https://github.com/jlleitschuh/ktlint-gradle) to enforce Kotlin code style.

To run the linter, run `./gradlew ktlintCheck`
To run the formatter, run `./gradlew ktlintFormat`

The codeStyles found in `.idea/codeStyles` are used by Android Studio to enforce the code style
(taken from https://pinterest.github.io/ktlint/rules/configuration-intellij-idea/)

