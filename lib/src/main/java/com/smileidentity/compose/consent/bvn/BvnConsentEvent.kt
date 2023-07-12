package com.smileidentity.compose.consent.bvn

sealed interface BvnConsentEvent {
    class SubmitBVNMode(val bvn: String) : BvnConsentEvent
    class SelectOTPDeliveryMode(val otpDeliveryMode: OtpDeliveryMode) : BvnConsentEvent
    object GoToSelectOTPDeliveryMode : BvnConsentEvent
    class SubmitOTPMode(val otp: String) : BvnConsentEvent
}

enum class OtpDeliveryMode {
    EMAIL, SMS
}
