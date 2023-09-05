package com.smileidentity.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.compose.consent.bvn.BvnOtpVerificationMode
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.BvnTotpModeRequest
import com.smileidentity.models.BvnTotpRequest
import com.smileidentity.models.JobType
import com.smileidentity.models.SubmitBvnTotpRequest
import com.smileidentity.util.createBvnOtpVerificationModes
import com.smileidentity.util.getExceptionHandler
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
    val isBvnValid: Boolean = false,
    val isBvnOtpValid: Boolean = false,
    val sessionId: String = "",
    val showLoading: Boolean = false,
    val showError: Boolean = false,
    val showSuccess: Boolean = false,
    val selectedBvnOtpVerificationMode: BvnOtpVerificationMode? = null,
    val bvnVerificationModes: List<BvnOtpVerificationMode> = listOf(),
)

const val bvnNumberLength = 11
const val bvnOtpLength = 6

internal class BvnConsentViewModel(
    private val userId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BvnConsentUiState())
    val uiState = _uiState.asStateFlow()

    var bvnNumber by mutableStateOf("")
        private set

    var otp by mutableStateOf("")
        private set

    var otpSentBy by mutableStateOf("")
        private set

    internal fun updateBvnNumber(input: String) {
        _uiState.update { it.copy(isBvnValid = input.length == bvnNumberLength) }
        bvnNumber = input
    }

    internal fun updateOtp(input: String) {
        _uiState.update { it.copy(isBvnOtpValid = input.length == bvnOtpLength) }
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
                userId = userId,
                jobType = JobType.BVN,
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val request = BvnTotpRequest(
                idNumber = bvnNumber,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val response = SmileID.api.requestBvnTotpMode(request = request)
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
                userId = userId,
                jobType = JobType.BVN,
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val request = BvnTotpModeRequest(
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
                userId = userId,
                jobType = JobType.BVN,
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val request = SubmitBvnTotpRequest(
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
