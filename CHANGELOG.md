# Changelog

## 10.0.0-beta03 (Unreleased)

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
- Breaking: renamed SmartSelfie Registration to SmartSelfie Enrollment
- Compile against API level 34
- Bump AndroidX Activity to 1.7.2
- Bump AndroidX Fragment to 1.6.0
- Bump Gradle to 8.0.2
- Tweak selfie progress indicator animation
- Minor update to default colors to add contrast
- Submit color liveness images instead of grayscale 
- Update SmartSelfie™ directives copy to be more succinct
- Changed the order of arguments in Composables to ensure required arguments come first and so that
  Modifier is the first optional argument

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
