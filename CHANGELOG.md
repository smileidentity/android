# Changelog

## Unreleased
- Bump CameraX to 1.3.0

## 10.0.3
### Added
- Exposed individual components as Composables

### Fixed
- Fixed missing `allowNewEnroll` in Document Verification Fragment

## 10.0.2

### Added
- Added `allowNewEnroll` on SmartSelfie, BiometricKYC, DocV and EnhancedDocV

## 10.0.1

### Fixed
- Marked `kotlinx-collections-immutable` as an `api` dependency

## 10.0.0

### Fixed
- Made `code` nullable on `SmileIDException.Details`

### Changed
- Bump Kotlin to 1.9.21
- Bump Sentry to 7.0.0

### Removed
- Removed `model_parameters` from `PrepUploadRequest`

## 10.0.0-beta14

### Added
- Added missing `showInstructions` on some Composables
- Added missing proguard rule and updated consumer rules
- Added missing parameters on Fragments

### Fixed
- Fixed crash when duplicate images are attempted to be zipped
- Fixed a bug where some attributes passed in were not respected
- Fixed a bug when attempting to parcelize `SmileIDException`

### Changed
- Bump Kotlin to 1.9.20
- Bump Compose BOM to 2023.10.01
- Bump AndroidX Activity to 1.8.1
- Bump AndroidX Fragment to 1.6.2
- Bump AndroidX Navigation to 2.7.5
- Bump Sentry to 6.33.1
- Bump Coil to 2.5.0
- Changed the OKHTTP call timeout to 60 seconds 
- Rename `partnerParams` to `extraPartnerParams`

## 10.0.0-beta13

### Added
- Added `partnerParams` as optional params on all job types
- Added `allowAgentMode` option on Document Verification and Enhanced Document Verification

## 10.0.0-beta12

### Fixed
- Fixed a bug where the document preview showed a black box for some older devices

## 10.0.0-beta11

### Fixed
- Fixed retry document submission on failed document submission
- Fixed missing `entered` key in BiometricKYC

## 10.0.0-beta10

### Added
- Added `jobId` on `SmartSelfieEnrollmentFragment` and `SmartSelfieAuthenticationFragment`
- Added `showInstructions` on `SmartSelfieEnrollmentFragment`

### Fixed
- Fix bug where `showAttirubtion` was not respected on the Consent Denied screen

### Changed
- Increased selfie capture resolution to 640px
- Bump Sentry to 6.32.0
- Bump OkHttp to 4.12.0

## 10.0.0-beta09

### Added
- Added missing `colorScheme` and `typography` parameters on `SmileID.BvnConsentScreen`
- Added option to set callback URL using `setCallbackUrl(callbackUrl: URL?)` method
- Added EnhancedDocumentVerification support
- Added Modifier parameter to all Composables

### Fixed
- Updated KDocs with missing parameter descriptions
- Fix Broken Country Selection on EnhancedKYC and BiometricKYC
- Update Window Insets to avoid content being drawn under system insets

### Changed
- Show the normal consent screen as part of BVN Verification
- The parameters of BvnConsentScreen have been updated to support inclusion of the consent screen
- Bump Compose BOM to 2023.09.02
- Bump AGP to 8.1.2
- Bump Sentry to 6.30.0

## 10.0.0-beta08

### Added
- Global Document Verification support
- BVN Consent Screen
- Dependency on `org.jetbrains.kotlinx:kotlinx-collections-immutable`

### Fixed
- Expose Compose Material 3 as an `api` dependency
- A bug where all results were being parsed to `JobResult.Entry`

### Changed
- Made `*Result` classes JSON serializable
- Renamed `DocVJobStatusResponse` to `DocumentVerificationJobStatusResponse`
- Renamed `getDocVJobStatus` to `getDocumentVerificationJobStatus`
- Renamed `pollDocVJobStatus` to `pollDocumentVerificationJobStatus`
- New sealed interface hierarchy for JobResult
  - Renamed `DocVEntry` to `DocumentVerificationJobResult.Entry`
  - Renamed `JobResult.Entry` to `SmartSelfieJobResult.Entry`
  - Renamed `BiometricKycEntry` to `BiometricKycJobResult.Entry`
  - `JobResult.Entry` is now an interface for all job types
- Bump Sentry to 6.29.0
- Bump Compose BOM to 2023.09.01
- Bump AndroidX Core to 1.12.0
- Bump AndroidX Lifecycle to 2.6.2
- Bump AndroidX Navigation to 2.7.2

### Removed
- Removed `Document` model, so you now pass `countryCode` and `documentType` as separate params in
  `SmileID.DocumentVerification`
- `filename` property from `PrepUploadRequest`, as it is no longer required

## 10.0.0-beta07

### Added
- Added BVN Verification for Nigeria
- Detection of bad lighting for Document Verification
- Detection of unfocused states for Document Verification
- Document detection for Document Verification

### Fixed
- Fix a Document Verification bug where selfie wasn't captured when also capturing the back of an ID
- Fixed a bug where the document bounding box border was slightly offset from document cutout
- Fixed a bug where the wrong `ImageType` was being specified for back of ID images

### Changed
- `resultCode`s, `code`s, and `BankCode.code`s are all now `String`s in order to maintain leading 0s
- Include liveness images for Document Verification jobs, if selfie capture was not bypassed
- Slightly zoom in on the document capture preview and confirmation
- Kotlin 1.9.10
- Bump Compose BOM to 2023.08.00
- Bump CameraX to 1.2.3
- Bump AndroidX Navigation to 2.7.1

## 10.0.0-beta06

### Fixed
- Added OkHttp as an `api` dependency
- Updated `LoadingButton` visibility modifier

### Changed
- Switch from Java 17 to Java 11 to support Flutter
- Allow passing in a custom `Config` instance to `SmileID.initialize`  
- Bump coroutines to 1.7.3
- Bump Sentry to 6.28.0
- Bump AndroidX Fragment to 1.6.1

## 10.0.0-beta05

### Added
- Add helper functions which return a Flow of the latest JobStatus for a Job until it is complete
- Add a `JobStatusResponse` interface
- Enhanced KYC Async API endpoint

### Fixed
- Fixed a bug where `id_info` was not included in Document Verification network requests

### Changed
- Kotlin 1.9
- Updated API key exception error message to be actionable
- `SmileID.useSandbox` getter is now publicly accessible
- Bump Sentry to 6.25.2

### Removed
- Removed polling from SmartSelfie Authentication, Document Verification, and Biometric KYC. The
  returned `SmileIDResult`s will now contain only the immediate result of job status without waiting
  for job completion

## 10.0.0-beta04

### Added
- Biometric KYC Fragment
- Made IdInfo Parcelable
- Option to disable Instructions Screen on Document Verification and SmartSelfie™
- Smile ID Attribution

### Fixed
- Fixed bug where Document Captures were incorrectly cropped
- Marked `selfieFile` as a required field in the returned `DocumentVerificationResult` 

### Changed
- Updated KDocs
- Rename `ImageType` enums to indicate PNGs are no longer supported 
- Bump Gradle to 8.2.1
- Bump Coroutines to 1.7.2
- Bump Sentry to 6.25.0

## 10.0.0-beta03

### Added
- Biometric KYC
- Document Verification
- Debounce Selfie Capture directive changes to allow user time to read the directions
- Set Release property on Sentry for release tracking
- Products Config API call
- Services API call

### Fixed
- Fix crash on network retries
- Don't report IDE Jetpack Compose Preview crashes to Sentry

### Changed
- Breaking: Renamed SmartSelfie Registration to SmartSelfie Enrollment
- Breaking: Removed "Screen" suffix from SmartSelfie Composables
- Tweak selfie progress indicator animation
- Minor update to default colors to add contrast
- Submit color liveness images instead of grayscale 
- Update SmartSelfie™ directives copy to be more succinct
- Changed the order of arguments in Composables to ensure required arguments come first and so that
  Modifier is the first optional argument
- Compile against API level 34
- Bump Gradle to 8.0.2
- Bump Kotlin to 1.8.22
- Bump AndroidX Activity to 1.7.2
- Bump AndroidX Fragment to 1.6.0
- Bump Compose BOM to 2023.06.01
- Bump Camposer to 0.2.2
- Bump Sentry to 6.24

## 10.0.0-beta02

### Added
- Add Biometric KYC data models
- Initial Document Verification screens

### Fixed
- Only allow a single face to be in frame for a SmartSelfie™ capture

### Changed
- Bump Sentry to 6.21.0

## 10.0.0-beta01

### Added
- Initial release 🎉
- SmartSelfie™ Authentication and Registration
- Enhanced KYC
- Theming
- Networking

### Dependencies
- Retrofit
- Moshi
- Timber
- AndroidX
  - Camera
  - Compose
  - Core
  - Fragment
  - Lifecycle
- Sentry
- Accompanist
- Camposer
