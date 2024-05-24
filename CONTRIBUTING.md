## Overview

This repo encompasses everything necessary for the Smile ID Android SDK. It is a multi-module
project consisting of the following modules:

- [`lib`](https://github.com/smileidentity/android/tree/main/lib) -
  The SDK distributed to partners
- [`sample`](https://github.com/smileidentity/android/tree/main/sample) - a sample app
  that demonstrates SDK integration and showcases Smile ID products

## Setup

- JDK 17+
- Android Studio 2022.2.1 (Flamingo) or higher
- Android Gradle Plugin 8.0.0 or higher
- Android SDK 34
- Add `SENTRY_DSN=<Android project Sentry DSN from Sentry Portal>` to `~/.gradle/gradle.properties`
- Add `smile_config.json` to the Sample App's assets (`sample/src/main/assets`)

## Building

- `./gradlew build` - builds, lints, and tests all modules

## Linting and Formatting

We use [ktlint](https://github.com/pinterest/ktlint) via
[this plugin](https://github.com/jlleitschuh/ktlint-gradle) to enforce Kotlin code style.

To run the linter, run `./gradlew ktlintCheck`. To run the formatter, run `./gradlew ktlintFormat`

ktLint uses the `.editorconfig` file to enforce code style. 
In addition, the codeStyles found in `.idea/codeStyles` are used by Android Studio to enforce the
code style (https://pinterest.github.io/ktlint/1.1.1/rules/configuration-intellij-idea/)

## Releasing

Generally speaking, releases should be performed by workflow dispatch. This will push a tag and
create a GitHub Release and publish to Maven Central. The SDK version is determined by the `version`
variable
in [`lib/lib.gradle.kts`](https://github.com/smileidentity/android/blob/main/lib/lib.gradle.kts)

That variable's value will be the `VERSION_NAME` Gradle Property, if defined, or the `lib/VERSION`
file otherwise.

Versions ending in `-SNAPSHOT` will be published to Sonatype's snapshot
[repository](https://oss.sonatype.org/content/repositories/snapshots/com/smileidentity/android-sdk/)

Otherwise, the version will be released as a production build to
[Maven Central](https://repo1.maven.org/maven2/com/smileidentity/android-sdk/). Note that it can
take up to 30 minutes for a production release to sync with Maven Central.

To publish, run the command `./gradlew publish`

Manual publishing requires setup:

### Create a Sonatype Account

1. Visit https://issues.sonatype.org/secure/Dashboard.jspa and Sign Up
2. Request access to the `com.smileidentity` Group ID by commenting on this JIRA Issue:
   https://issues.sonatype.org/browse/OSSRH-50589. Alternatively, create a new JIRA Issue

### Generate a GPG Key Pair

Follow the instructions at https://central.sonatype.org/publish/requirements/gpg/
The summary is:

1. Install GPG
2. Generate a GPG Key (`gpg —-gen-key`)
3. List the generated key and determine the Key ID (`gpg --list-keys --keyid-format LONG`)
4. Distribute the public key to a well-known keyserver
   (`gpg --keyserver keys.openpgp.org --send-keys KEY_ID`)
5. Verify key was distributed (`gpg --keyserver keys.openpgp.org --search-key KEY_ID`)

### Configure Play Store Upload Keystore

The upload keystore lives in 1Password. It is named `Play Store Upload Keystore`. Download this file
and save it to `sample/upload.jks`.

It is also stored in this project as a Base64 encoded secret for use by GitHub Actions.

### Configure Gradle Properties

Add the following properties to `~/.gradle/gradle.properties`:

```properties
# suppress inspection "UnusedProperty" for whole file
mavenCentralUsername=<Your Maven Central Username>
mavenCentralPassword=<Your Maven Central Password>
signing.keyId=<Your GPG Key ID>
signing.password=<Your GPG Key Password>
signing.secretKeyRingFile=<Your GPG Keyring path (e.g. ~/.gnupg/secring.gpg)>
uploadKeystorePassword=<The password for the upload keystore>
```

## Other Notes

- When adding new resources, please use the `si_` prefix to avoid conflicts with other libraries and
  applications
- When adding drawables, please compress them as much as possible (tinypng.com is a good resource
  for this) as well as `avocado` (`npm install -g avocado`) for compressing android vector drawables
- When adding vector drawables, please check that evenOdd fillType is not used unnecessarily. Using
  evenOdd causes build time png generation, because evenOdd requires API 24 (our minSdk is 21)
- Any new developer should add themselves to the the `developers` block in
  [`lib/lib.gradle.kts`](https://github.com/smileidentity/android/blob/main/lib/lib.gradle.kts)
- The linter enforces a maximum line length of 100 characters. Please try to keep lines under this
  length. However, if it is not possible (i.e. a long resource name), you can disable the check for
  the line by adding `// ktlint-disable max-line-length` to the end of the line
- You can obtain Compose Compiler metrics (i.e. to debug performance or recomposition issues) by
  viewing the reports saved to `lib/build/compose_compiler` and `sample/build/compose_compiler`
- It is recommended to set up a pre-commit hook (ktLint - formatting, lint - Composable lints). To
  do so, add the following to `.git/hooks/pre-commit`:
  ```
  #!/usr/bin/env sh
  echo "Running pre-commit hook"
  ./gradlew ktlintCheck lint --daemon
  STATUS=$?
  [ $STATUS -ne 0 ] && echo "Lint failed. Run ./gradlew ktlintFormat and check IDE lints" && exit 1
  echo "Lint passed"
  ```

## FAQs

- Q: Why are source sets are located in `$module/src/main/java`, even though we're using Kotlin?
    - A: To simplify configuration. `$module/src/main/kotlin` would require custom build setup. And
      at the end of the day, it doesn't really matter. Kotlin files can go in `java` just fine
- Q: Why can't I see coverage info inside Android Studio?
    - A: https://issuetracker.google.com/issues/241258667
- Q: How come I can't preview Markdown files within Android Studio?
    - A: You need to change the JetBrains Runtime to the latest version **with JCEF**. see
      also: https://stackoverflow.com/a/71063742
