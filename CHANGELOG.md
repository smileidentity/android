# Changelog

## 10.0.0-beta04 (unreleased)

### Added
- Biometric KYC Fragment
- Made IdInfo Parcelable

### Fixed

### Changed
- Updated KDocs
- Bump Gradle to 8.2
- Rename `ImageType` enums to indicate PNGs are no longer supported 

### Removed

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
