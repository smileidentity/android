package com.smileidentity.compose.consent.bvn

sealed interface BvnConsentEvent {
    class SubmitBVNMode(
        val bvn: String,
        val country: String,
        val idType: String,
    ) : BvnConsentEvent

    class SelectOTPDeliveryMode(
        val otpDeliveryMode: OtpDeliveryMode,
        val otpSentTo: String,
        val country: String,
        val idNumber: String,
        val idType: String,
        val sessionId: String,
    ) : BvnConsentEvent

    object GoToSelectOTPDeliveryMode : BvnConsentEvent
    class SubmitBvnOtp(
        val otp: String,
        val country: String,
        val idNumber: String,
        val idType: String,
        val sessionId: String,
    ) : BvnConsentEvent
}

enum class OtpDeliveryMode {
    EMAIL, SMS
}

enum class BvnCountry(val country: String) {
    NIGERIA("NG"),
}

enum class CountryIdType(val idType: String) {
    NIGERIA_BVN("BVN_MFA"),
}
