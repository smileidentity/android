package com.smileidentity.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.compose.consent.bvn.BvnOtpVerificationMode
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.BvnToptModeRequest
import com.smileidentity.models.BvnToptRequest
import com.smileidentity.models.JobType
import com.smileidentity.models.SubmitBvnToptRequest
import com.smileidentity.util.createBvnOtpVerificationModes
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.randomUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

internal enum class BvnConsentScreens {
    BvnInputScreen,
    ChooseOtpDeliveryScreen,
    ShowVerifyOtpScreen,
    ShowWrongOtpScreen,
}

internal data class BvnConsentUiState(
    val bvnConsentScreens: BvnConsentScreens = BvnConsentScreens.BvnInputScreen,
    val sessionId: String = "",
    val showLoading: Boolean = false,
    val showError: Boolean = false,
    val showSuccess: Boolean = false,
    val selectedBvnOtpVerificationMode: BvnOtpVerificationMode? = null,
    val bvnVerificationModes: List<BvnOtpVerificationMode> = listOf(),
)

internal class BvnConsentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BvnConsentUiState())
    val uiState = _uiState.asStateFlow()

    var bvnNumber by mutableStateOf("")
        private set

    var otp by mutableStateOf("")
        private set

    var otpSentBy by mutableStateOf("")
        private set

    internal fun updateBvnNumber(input: String) {
        bvnNumber = input
    }

    internal fun updateOtp(input: String) {
        otp = input
    }

    internal fun updateMode(input: BvnOtpVerificationMode) {
        _uiState.update { it.copy(selectedBvnOtpVerificationMode = input) }
        otpSentBy = input.otpSentBy
    }

    internal fun selectContactMethod() {
        _uiState.update { it.copy(bvnConsentScreens = BvnConsentScreens.ChooseOtpDeliveryScreen) }
        otp = ""
    }

    internal fun submitUserBvn() {
        _uiState.update { it.copy(showLoading = true) }
        val proxy = { e: Throwable ->
            Timber.e(e)
            _uiState.update { it.copy(showError = true, showLoading = false) }
        }

        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                userId = randomUserId(),
                jobType = JobType.BVN, // TODO - What JobType?
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val request = BvnToptRequest(
                idNumber = bvnNumber,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val response = SmileID.api.requestBvnOtpMode(request = request)
            _uiState.update {
                it.copy(
                    bvnConsentScreens = BvnConsentScreens.ChooseOtpDeliveryScreen,
                    showLoading = false,
                    sessionId = response.sessionId,
                    showError = false,
                    bvnVerificationModes = createBvnOtpVerificationModes(response.modes),
                )
            }
        }
    }

    internal fun requestBvnOtp() {
        _uiState.update { it.copy(showLoading = true) }
        val proxy = { e: Throwable ->
            Timber.e(e)
            _uiState.update { it.copy(showLoading = false) }
        }

        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                userId = randomUserId(),
                jobType = JobType.BVN, // TODO - What JobType?
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val request = BvnToptModeRequest(
                idNumber = bvnNumber,
                mode = otpSentBy,
                sessionId = uiState.value.sessionId,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val response = SmileID.api.requestBvnOtp(request = request)
            _uiState.update {
                it.copy(
                    bvnConsentScreens = BvnConsentScreens.ShowVerifyOtpScreen,
                    showLoading = false,
                    showSuccess = response.success,
                )
            }
        }
    }

    internal fun submitBvnOtp() {
        _uiState.update { it.copy(showLoading = true) }
        val proxy = { e: Throwable ->
            Timber.e(e)
            _uiState.update {
                it.copy(
                    showLoading = false,
                    bvnConsentScreens = BvnConsentScreens.ShowWrongOtpScreen,
                )
            }
        }

        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                userId = randomUserId(),
                jobType = JobType.BVN, // TODO - What JobType?
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val request = SubmitBvnToptRequest(
                idNumber = bvnNumber,
                otp = otp,
                sessionId = uiState.value.sessionId,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val response = SmileID.api.submitBvnOtp(request = request)
            _uiState.update { it.copy(showLoading = false, showSuccess = response.success) }
        }
    }
}
