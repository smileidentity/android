# Release Notes

# 11.1.0 - July 31, 2025

### Added

* Added Face Tracking in Selfie Capture to ensure only same face is used during capture
* Added `autoCaptureTimeout` that allows partners to override the default 10 seconds auto capture
  timeout
* Added new wrapper name `ReactNativeExpo` for React Native Expo SDK.

### Changed

* Removed `AntiFraud` response in `JobStatus` calls
* Updated the `targetSdk` to 36 and updated the AGP version
* Removed the default `ConsentInformation` set when no consent is passed
* Changed `enableAutoCapture` to `AutoCapture` enum to allow to allow partners change document
  capture options
* Fixed a bug on the `SelfieCaptureScreen` where the instruction text would overlap with the face
  cutout and also fixed padding issues when agent mode hidden

## 11.0.5 - July 2, 2025

### Added

* Added option to disable document auto capture in DocV and Enhanced DocV

## 11.0.4 - June 16, 2025

### Changed

* Migrated from tflite to litert
* Updated to v1.0.1 of security sdk

## 11.0.3

### Changed

* Fix/expose localmetadata provider

## 11.0.2

### Fixed

* Fixed a bug with geolocation

## 11.0.1

### Changed

* Updated Sentry version from 7.2.0 to the latest(8.13.2). Replaced the use of Sentry’s Hub-based
  implementation with the new Scope and Scopes-based implementation.

## 11.0.0

### Added

* Added a security feature to protect the payload between sdk and backend from unauthorized
  tampering.
* Enhanced fraud signals collection to improve fraud prevention

### Changed

* Changed File save path from cacheDir to fileDir (and maintain compatibility till next major
  breaking release)

### Fixed

* Fixed a bug where a filepath wasn't updated on a retry of a document verification job

## 10.6.3

### Fixed

* Fixed `showAttribution` parameter not being passed to the instruction screen in enhanced selfie
  capture

## 10.6.2

### Changed

* Restructured consent object that is being sent to the backend API for biometric kyc, enhanced kyc
  and enhanced document verification

## Unreleased

### Added

* Added a security feature to protect the payload between sdk and backend from unauthorized
  tampering.

## 10.6.1

* Changed Enhanced SmartSelfie™ viewmodels to call `onResult` when the user clicks cancel instead of
  when there is an error
* Added messages from SmartSelfie™ errors will show correctly if present and will show default
  failure message if not present
* Updated the preview to throw SmileIDException and not crash if the file is null

## 10.6.0

* Changes the `allow_new_enroll` flag to be a real boolean instead of a string for prepUpload
  requests and multi-part requests. This is a breaking change for stored offline jobs, where the job
  is written using an older sdk version and then submission is attempted using this version.

## 10.5.2

* Added Enhanced SmartSelfie™ Capture for enroll and authentication fragments
* Added an option to capture a selfie without submitting to the API. This is available in Enhanced
  SmartSelfie™ and SmartSelfie™ capture flows by setting `skipApiSubmission=true` in enroll and
  authentication products.

## 10.5.1

* Make ConsentInformation optional in EnhancedDocV, EnhancedKYC and BiometricKYC
* Add strict mode to enable Enhanced SmartSelfie™ capture on the BiometricKYC,
  DocumentVerification and EnhancedDocV fragments

## 10.5.0

* Pass ConsentInformation in EnhancedDocV, EnhancedKYC and BiometricKYC
* Timestamp consistency from date epoch to iso format
* Removed network retries on OKHTTP
* Updated timeouts to 60 seconds
* Add enhancedSelfieCapture to the BiometricKYC, DocumentVerification and EnhancedDocV

## 10.4.2

* Android correct variable names for to match smile_config.json

## 10.4.1

* Throw IOException in the SmileIDResult.Error class
* Fixed document back file returned as null in the results for DocV and Enhanced DocV

## 10.4.0

* Introduce screens for the new Enhanced Selfie Capture Enrollment and Authentication Products.
* Fixed inconsistent document type parameters on sample app

## 10.3.7

* Fixed extraPartnerParams serialization issues

## 10.3.6

* Modify access for document capture and selfie capture
* Allow skipApiSubmission for both document capture and selfie capture flows to allow for capture
  and return of file paths without submitting the job to SmileID

## 10.3.5

* Fix the camera is closed bug on document capture flow
* Enhanced doc v submitted as doc v
* Correct flow for selfie when showInstructions is false

## 10.3.4

* Bump up the compileSdk to 35
* Add skipApiSubmission when true will capture smartselfie and return file paths for selfie and
  liveness images without submitting the job to SmilID

## 10.3.3

* Added inflow navigation as well as individual navigation for compose screens

## 10.3.2

* Document capture throw OOM when encountered

## 10.3.1

* Fix insecure object serialization on fragments

## 10.3.0

* Changed `initialize()` to return a deferred result (allow partners to handle errors)
* Update to Compose Fragment and remove ComposeView

## 10.2.7

* Fixed upload bug to retry in case a job already exists but zip was not uploaded
* Changed MLKit download modules to throw an exception explicitly if download fails

## 10.2.6

* Make document captured image confirmation to be optional

## 10.2.5

* Fixed a bug where prep upload would not work for previously attempted API requests

## 10.2.4

#### Fixed

* Job status history full data parsing causing a crash during polling

### Changed

* Removed `SmileID.setEnvironment()` since the API Keys are no longer shared between environments

## 10.2.3

* Handle invalid resource IDs
* Fix fileSavePath not initialized error (missing smile_config.json file)
* Add missing ActionResult responses

## 10.2.2

* Fixed a bug where the `BiometricKycViewModel` would succeed but use the default error message as
  it was not being updated when changing state
* Fixed a bug where `SmileID.submitJob` would not work for previously attempted API requests

## 10.2.1

* Wrap Composables in a `Surface` for additional background color customization
* Add metadata support

## 10.2.0

* Added an optional "Strict Mode" to SmartSelfie Enrollment and Authentication to achieve better
  pass rates. Set `useStrictMode=true` to enable this new, streamlined UI and associated active
  liveness tasks

## 10.1.7

* Fixed a bug where some failed authentication requests were incorrectly handled
* Fixed a bug where errors with no code were not being handled correctly
* Fixed a bug on Selfie and Document capture success screen where the message was wrong
* Fixed a bug where liveness files were missing on document verification jobs

## 10.1.6

* Update generic errors with actual platform errors

## 10.1.5

* Fixed a bug where MLKit initialization would sometimes fail due to Application Context
* Verify SDK for Google Play SDK Console

## 10.1.4

* Increase network call timeout to 120 seconds
* Fixed a bug where invalid file paths were returned and retries did not work
* Update to K2 (aka Kotlin `2.0.0`)
* Update Compose BOM to 2024.05.00
* Update AndroidX to 1.13.1
* Update Activity to 1.9.0
* Update Fragment to 1.7.1
* Update Datastore to 1.1.1
* Update Sentry to 7.9.0

## 10.1.2

* Better error message when the device is low on storage
* Fixed a bug where Gallery selection of document images did not work in some cases

## 10.1.1

* Fixed a bug where Document Capture would occasionally fail
* Optimize Bitmap processing for less memory usage and improved quality
* Fixed a bug where Document Verification would get stuck on the processing screen

## 10.1.0

* Added an Offline Mode, enabled by calling `SmileID.setAllowOfflineMode(true)`. If a job is
  attempted while the device is offline, and offline mode has been enabled, the UI will complete
  successfully and the job can be submitted at a later time by calling `SmileID.submitJob(jobId)`
* Improved SmartSelfie Enrollment and Authentication times by moving to a synchronous API endpoint
* Made `KEY_RESULT` constants in `Fragment`s `internal` to remove a footgun where the constant was
  easily confused with `KEY_REQUEST`
* Improved back button behavior on image confirmation and processing dialogs
* Fixed a bug where network retries would occasionally fail
* Bump Compose BOM to 2024.04.00

## 10.0.4

* Bump CameraX to 1.3.0
* Removed `cameraConfig` from `PrepUploadResponse`
* Fixed a bug where, in some cases, logs may not be printed
* Removed the Skip Button from Back of ID Capture
* Added `instructionsHeroImage` as a new parameter to `DocumentCaptureScreen`
* Added `heroImage` as a new parameter to `DocumentCaptureInstructionsScreen`
* Updated Document Verification hero images

## 10.0.3

* Exposed individual components as Composables
* Fixed missing `allowNewEnroll` in Document Verification Fragment

## 10.0.2

* Added `allowNewEnroll` on SmartSelfie, BiometricKYC, DocV and EnhancedDocV

## 10.0.1

### Fixed

* Marked `kotlinx-collections-immutable` as an `api` dependency

## 10.0.0

### Fixed

* Made `code` nullable on `SmileIDException.Details`

### Changed

* Bump Kotlin to 1.9.21
* Bump Sentry to 7.0.0

### Removed

* Removed `model_parameters` from `PrepUploadRequest`

## 10.0.0-beta14

### Added

* Added missing `showInstructions` on some Composables
* Added missing proguard rule and updated consumer rules
* Added missing parameters on Fragments

### Fixed

* Fixed crash when duplicate images are attempted to be zipped
* Fixed a bug where some attributes passed in were not respected
* Fixed a bug when attempting to parcelize `SmileIDException`

### Changed

* Bump Kotlin to 1.9.20
* Bump Compose BOM to 2023.10.01
* Bump AndroidX Activity to 1.8.1
* Bump AndroidX Fragment to 1.6.2
* Bump AndroidX Navigation to 2.7.5
* Bump Sentry to 6.33.1
* Bump Coil to 2.5.0
* Changed the OKHTTP call timeout to 60 seconds
* Rename `partnerParams` to `extraPartnerParams`

## 10.0.0-beta13

### Added

* Added `extras` as optional params on all job types
* Added `allowAgentMode` option on Document Verification and Enhanced Document Verification

## 10.0.0-beta12

### Fixed

* Fixed a bug where the document preview showed a black box for some older devices

## 10.0.0-beta11

#### Fixed

* Fixed retry document submission on failed document submission
* Fixed missing `entered` key in BiometricKYC

## 10.0.0-beta10

#### Added

* Added `jobId` on `SmartSelfieEnrollmentFragment` and `SmartSelfieAuthenticationFragment`
* Added `showInstructions` on `SmartSelfieEnrollmentFragment`

#### Fixed

* Fix bug where `showAttirubtion` was not respected on the Consent Denied screen

#### Changed

* Increased selfie capture resolution to 640px
* Bump Sentry to 6.32.0
* Bump OkHttp to 4.12.0

## 10.0.0-beta09

#### Added

* Added missing `colorScheme` and `typography` parameters on `SmileID.BvnConsentScreen`
* Added option to set callback URL using `setCallbackUrl(callbackUrl: URL?)` method
* Added EnhancedDocumentVerification support
* Added Modifier parameter to all Composables

#### Fixed

* Updated KDocs with missing parameter descriptions
* Fix Broken Country Selection on EnhancedKYC and BiometricKYC
* Update Window Insets to avoid content being drawn under system insets

#### Changed

* Show the normal consent screen as part of BVN Verification
* The parameters of BvnConsentScreen have been updated to support inclusion of the consent screen
* Bump Compose BOM to 2023.09.02
* Bump AGP to 8.1.2
* Bump Sentry to 6.30.0

## 10.0.0-beta08

#### Added

* Global Document Verification support
* BVN Consent Screen
* Dependency on `org.jetbrains.kotlinx:kotlinx-collections-immutable`

#### Fixed

* Expose Compose Material 3 as an `api` dependency
* A bug where all results were being parsed to `JobResult.Entry`

#### Changed

* Made `*Result` classes JSON serializable
* Renamed `DocVJobStatusResponse` to `DocumentVerificationJobStatusResponse`
* Renamed `getDocVJobStatus` to `getDocumentVerificationJobStatus`
* Renamed `pollDocVJobStatus` to `pollDocumentVerificationJobStatus`
* New sealed interface hierarchy for JobResult
    * Renamed `DocVEntry` to `DocumentVerificationJobResult.Entry`
    * Renamed `JobResult.Entry` to `SmartSelfieJobResult.Entry`
    * Renamed `BiometricKycEntry` to `BiometricKycJobResult.Entry`
    * `JobResult.Entry` is now an interface for all job types
* Bump Sentry to 6.29.0
* Bump Compose BOM to 2023.09.01
* Bump AndroidX Core to 1.12.0
* Bump AndroidX Lifecycle to 2.6.2
* Bump AndroidX Navigation to 2.7.2

#### Removed

* Removed `Document` model, so you now pass `countryCode` and `documentType` as separate params in
  `SmileID.DocumentVerification`
* `filename` property from `PrepUploadRequest`, as it is no longer required

## 10.0.0-beta07

#### Added

* Detection of bad lighting for Document Verification
* Detection of unfocused states for Document Verification
* Document detection for Document Verification

#### Fixed

* Fix a Document Verification bug where selfie wasn't captured when also capturing the back of an ID
* Fixed a bug where the document bounding box border was slightly offset from document cutout
* Fixed a bug where the wrong `ImageType` was being specified for back of ID images

#### Changed

* `resultCode`s, `code`s, and `BankCode.code`s are all now `String`s in order to maintain leading 0s
* Include liveness images for Document Verification jobs, if selfie capture was not bypassed
* Slightly zoom in on the document capture preview and confirmation
* Kotlin 1.9.10
* Bump Compose BOM to 2023.08.00
* Bump CameraX to 1.2.3
* Bump AndroidX Navigation to 2.7.1

## 10.0.0-beta06

#### Fixed

* Added OkHttp as an `api` dependency
* Updated `LoadingButton` visibility modifier

#### Changed

* Switch from Java 17 to Java 11 to support Flutter
* Allow passing in a custom `Config` instance to `SmileID.initialize`
* Bump coroutines to 1.7.3
* Bump Sentry to 6.28.0
* Bump AndroidX Fragment to 1.6.1

## 10.0.0-beta05

#### Added

* Add helper functions which return a Flow of the latest JobStatus for a Job until it is complete
* Add a `JobStatusResponse` interface
* Enhanced KYC Async API endpoint

#### Fixed

* Fixed a bug where `id_info` was not included in Document Verification network requests

#### Changed

* Kotlin 1.9
* Updated API key exception error message to be actionable
* `SmileID.useSandbox` getter is now publicly accessible
* Bump Sentry to 6.25.2

#### Removed

* Removed polling from SmartSelfie Authentication, Document Verification, and Biometric KYC. The
  returned `SmileIDResult`s will now contain only the immediate result of job status without waiting
  for job completion

## 10.0.0-beta04

#### Added

* Biometric KYC Fragment
* Made IdInfo Parcelable
* Option to disable Instructions Screen on Document Verification and SmartSelfie™
* Smile ID Attribution

#### Fixed

* Fixed bug where Document Captures were incorrectly cropped
* Marked `selfieFile` as a required field in the returned `DocumentVerificationResult`

#### Changed

* Updated KDocs
* Rename `ImageType` enums to indicate PNGs are no longer supported
* Bump Gradle to 8.2.1
* Bump Coroutines to 1.7.2
* Bump Sentry to 6.25.0

## 10.0.0-beta03

#### Added

* Biometric KYC
* Document Verification
* Debounce Selfie Capture directive changes to allow user time to read the directions
* Set Release property on Sentry for release tracking
* Products Config API call
* Services API call

#### Fixed

* Fix crash on network retries
* Don't report IDE Jetpack Compose Preview crashes to Sentry

#### Changed

* **Breaking**: Renamed SmartSelfie Registration to SmartSelfie Enrollment
* **Breaking**: Removed "Screen" suffix from SmartSelfie Composables
* Tweak selfie progress indicator animation
* Minor update to default colours to add contrast
* Submit colour liveness images instead of greyscale
* Update SmartSelfie™ directives copy to be more succinct
* Changed the order of arguments in Composables to ensure required arguments come first and so that
  Modifier is the first optional argument
* Compile against API level 34
* Bump Gradle to 8.0.2
* Bump Kotlin to 1.8.22
* Bump AndroidX Activity to 1.7.2
* Bump AndroidX Fragment to 1.6.0
* Bump Compose BOM to 2023.06.01
* Bump Camposer to 0.2.2
* Bump Sentry to 6.24

## 10.0.0-beta02

#### Added

* Add Biometric KYC data models
* Initial Document Verification screens

#### Fixed

* Only allow a single face to be in frame for a SmartSelfie™ capture

#### Changed

* Bump Sentry to 6.21.0

## 10.0.0-beta01

* Initial Release
* SmartSelfie™ Authentication and Registration
* Enhanced KYC
* Theming
* Networking
