package com.smileidentity.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.BvnToptModeRequest
import com.smileidentity.models.BvnToptRequest
import com.smileidentity.models.BvnVerificationMode
import com.smileidentity.models.JobType
import com.smileidentity.models.SubmitBvnToptRequest
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.randomUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

enum class BvnConsentScreens {
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
    val bvnVerificationModes: List<BvnVerificationMode> = listOf(),
)

internal class BvnConsentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BvnConsentUiState())
    val uiState = _uiState.asStateFlow()

    var bvnNumber by mutableStateOf("")
        private set

    var otp by mutableStateOf("")
        private set

    var mode by mutableStateOf("")
        private set

    fun updateBvnNumber(input: String) {
        bvnNumber = input
    }

    fun updateOtp(input: String) {
        otp = input
    }

    fun updateMode(input: String) {
        mode = input
    }

    fun submitUserBvn() {
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
                    bvnVerificationModes = response.modes,
                )
            }
        }
    }

    fun requestBvnOtp() {
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
                mode = mode,
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

    fun submitBvnOtp() {
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
