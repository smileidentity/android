package com.smileidentity.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.consent.bvn.BvnConsentEvent
import com.smileidentity.compose.consent.bvn.OtpDeliveryMode
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.BvnToptModeRequest
import com.smileidentity.models.BvnToptRequest
import com.smileidentity.models.JobType
import com.smileidentity.models.SubmitBvnToptRequest
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.randomUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class BvnConsentUiState(
    val bvn: String = "",
    val sessionId: String = "",
    val showLoading: Boolean = false,
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
            is BvnConsentEvent.SubmitBVNMode -> setBvn(
                bvn = bvnConsentEvent.bvn,
                country = bvnConsentEvent.country,
                idType = bvnConsentEvent.idType,
            )

            is BvnConsentEvent.SelectOTPDeliveryMode ->
                requestBvnConsentOtp(
                    otpDeliveryMode = bvnConsentEvent.otpDeliveryMode,
                    otpSentTo = bvnConsentEvent.otpSentTo,
                    country = bvnConsentEvent.country,
                    idNumber = bvnConsentEvent.idNumber,
                    idType = bvnConsentEvent.idType,
                    sessionId = bvnConsentEvent.sessionId,
                )

            is BvnConsentEvent.SubmitBvnOtp -> submitBvnOtp(
                otp = bvnConsentEvent.otp,
                idNumber = bvnConsentEvent.idNumber,
                idType = bvnConsentEvent.idType,
                country = bvnConsentEvent.country,
                sessionId = bvnConsentEvent.sessionId,
            )

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

    private fun setBvn(
        bvn: String,
        country: String,
        idType: String,
    ) {
        _uiState.update { it.copy(showLoading = true) }
        val proxy = { e: Throwable ->
            Timber.e(e)
            _uiState.update {
                it.copy(
                    showLoading = false,
                    showWrongBvn = true,
                )
            }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                userId = randomUserId(),
                jobType = JobType.BVN,
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val bvnToptRequest = BvnToptRequest(
                country = country,
                idNumber = bvn,
                idType = idType,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val response = SmileID.api.requestBvnTotp(bvnToptRequest = bvnToptRequest)
            _uiState.update {
                it.copy(
                    showLoading = false,
                    sessionId = response.sessionId,
                )
            }
        }
    }

    private fun requestBvnConsentOtp(
        otpDeliveryMode: OtpDeliveryMode,
        otpSentTo: String,
        country: String,
        idNumber: String,
        idType: String,
        sessionId: String,
    ) {
        _uiState.update { it.copy(showLoading = true) }
        val proxy = { e: Throwable ->
            Timber.e(e)
            _uiState.update {
                it.copy(
                    showLoading = false,
                )
            }
        }

        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                userId = randomUserId(),
                jobType = JobType.BVN,
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val bvnToptModeRequest = BvnToptModeRequest(
                country = country,
                idNumber = idNumber,
                idType = idType,
                mode = otpDeliveryMode.name,
                sessionId = sessionId,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val response = SmileID.api.requestBvnTotpMode(bvnToptModeRequest = bvnToptModeRequest)
            if (response.success) {
                _uiState.update {
                    it.copy(
                        showLoading = false,
                        showDeliveryMode = false,
                        showOtpScreen = true,
                        otpSentTo = otpSentTo,
                    )
                }
            }
        }
    }

    private fun submitBvnOtp(
        country: String,
        idNumber: String,
        idType: String,
        otp: String,
        sessionId: String,
    ) {
        _uiState.update { it.copy(showLoading = true) }
        val proxy = { e: Throwable ->
            Timber.e(e)
            _uiState.update {
                it.copy(
                    showLoading = false,
                )
            }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                userId = randomUserId(),
                jobType = JobType.BVN,
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val submitBvnToptRequest = SubmitBvnToptRequest(
                country = country,
                idNumber = idNumber,
                idType = idType,
                otp = otp,
                sessionId = sessionId,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val response = SmileID.api.submitBvnTotp(submitBvnToptRequest = submitBvnToptRequest)
            _uiState.getAndUpdate {
                if (it.showWrongOtp) {
                    it.copy(
                        showOtpScreen = true,
                        showWrongOtp = true,
                        showExpiredOtpScreen = false,
                    )
                } else {
                    it.copy(
                        showOtpScreen = false,
                        showExpiredOtpScreen = true,
                        showWrongOtp = false,
                        errorMessage = R.string.si_bvn_consent_error_subtitle,
                    )
                }
            }
        }
    }
}
