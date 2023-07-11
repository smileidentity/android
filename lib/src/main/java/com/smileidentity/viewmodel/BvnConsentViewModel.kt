package com.smileidentity.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.smileidentity.R
import com.smileidentity.compose.consent.bvn.BvnConsentEvent
import com.smileidentity.compose.consent.bvn.OtpDeliveryMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

data class BvnConsentUiState(
    val bvn: String? = null,
    val showWrongBvn: Boolean = false,
    val showDeliveryMode: Boolean = true,
    val showOtpScreen: Boolean = false,
    val otpSentTo: String = "",
    val showWrongOtp: Boolean = false,
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
                // Reset the entire bvn consent state
                _uiState.update {
                    it.copy(
                        showDeliveryMode = true,
                        showOtpScreen = false,
                        otpSentTo = "",
                        showWrongOtp = false,
                        showExpiredOtpScreen = false,
                    )
                }
            }
        }
    }

    private fun setBvn(bvn: String) {
        // Do network call here
        _uiState.update {
            it.copy(
                bvn = bvn,
                showDeliveryMode = true,
            )
        }
    }

    private fun requestBvnConsentOtp(otpDeliveryMode: OtpDeliveryMode) {
        // API Implementation here - otpDeliveryMode enum can be improved

        // Disable the show delivery page
        _uiState.update { it.copy(showDeliveryMode = false) }

        // Show OTP Verification page
        _uiState.update { it.copy(showOtpScreen = true) }

        // Set value of where the OTP was sent
        _uiState.update { it.copy(otpSentTo = "ema**@example.com") }

        // If the OTP has expired
        _uiState.update { it.copy(errorMessage = R.string.si_bvn_consent_error_subtitle) }
    }

    private fun submitBvnOtp(otp: String) {
        // API Implementation here to submit the OTP

        // Update state based on the API response

        // mocked to test ui
        _uiState.getAndUpdate {
            if (it.showWrongOtp) {
                it.copy(
                    showOtpScreen = false,
                    showExpiredOtpScreen = true,
                    showWrongOtp = false,
                )
            } else {
                it.copy(
                    showOtpScreen = true,
                    showWrongOtp = true,
                    showExpiredOtpScreen = false,
                )
            }
        }
    }
}
