package com.smileidentity.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.smileidentity.R
import com.smileidentity.consent.bvn.BvnConsentEvent
import com.smileidentity.consent.bvn.OtpDeliveryMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BvnConsentUiState(
    val bvn: String? = null,
    val showDeliveryMode: Boolean = true,
    val showOtpScreen: Boolean = false,
    val showWrongOtpScreen: Boolean = false,
    val showExpiredOtpScreen: Boolean = false,
    @StringRes val errorMessage: Int? = null,
)

class BvnConsentViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BvnConsentUiState())
    val uiState = _uiState.asStateFlow()

    fun handleEvent(bvnConsentEvent: BvnConsentEvent) {
        when (bvnConsentEvent) {
            is BvnConsentEvent.SubmitBVNMode -> setBvn(bvn = bvnConsentEvent.bvn)
            is BvnConsentEvent.SelectOTPDeliveryMode ->
                requestBvnConsentOtp(otpDeliveryMode = bvnConsentEvent.otpDeliveryMode)

            is BvnConsentEvent.SubmitOTPMode -> submitBvnOtp(bvnConsentEvent.otp)
            BvnConsentEvent.GoToSelectOTPDeliveryMode -> {
                _uiState.update { it.copy(showDeliveryMode = true) }
            }
        }
    }

    private fun setBvn(bvn: String) {
        _uiState.update { it.copy(bvn = bvn) }
    }

    private fun requestBvnConsentOtp(otpDeliveryMode: OtpDeliveryMode) {
        // API Implementation here - otpDeliveryMode enum can be improved

        // If the OTP has expired
        _uiState.update { it.copy(errorMessage = R.string.si_bvn_consent_error_subtitle) }
    }

    private fun submitBvnOtp(otp: String) {
        // API Implementation here to submit the OTP

        // Update state based on the API response
    }
}
