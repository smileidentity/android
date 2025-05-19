package com.smileidentity.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.compose.consent.bvn.BvnOtpVerificationMode
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.BvnTotpModeRequest
import com.smileidentity.models.BvnTotpRequest
import com.smileidentity.models.JobType
import com.smileidentity.models.SubmitBvnTotpRequest
import com.smileidentity.util.createBvnOtpVerificationModes
import com.smileidentity.util.getExceptionHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

const val BVN_LENGTH = 11
const val BVN_OTP_LENGTH = 6

internal enum class BvnConsentScreens {
    ConsentScreen,
    BvnInputScreen,
    ChooseOtpDeliveryScreen,
    VerifyOtpScreen,
}

internal data class BvnConsentUiState(
    val currentScreen: BvnConsentScreens = BvnConsentScreens.ConsentScreen,
    val isBvnValid: Boolean = false,
    val isBvnOtpValid: Boolean = false,
    val sessionId: String = "",
    val showLoading: Boolean = false,
    val showError: Boolean = false,
    val showSuccess: Boolean = false,
    val selectedBvnOtpVerificationMode: BvnOtpVerificationMode? = null,
    val bvnVerificationModes: ImmutableList<BvnOtpVerificationMode> = persistentListOf(),
)

internal class BvnConsentViewModel(private val userId: String) : ViewModel() {
    private val _uiState = MutableStateFlow(BvnConsentUiState())
    val uiState = _uiState.asStateFlow()

    private var authResponse: AuthenticationResponse? = null

    private val errorProxy = { e: Throwable ->
        Timber.e(e)
        _uiState.update { it.copy(showError = true, showLoading = false) }
    }

    var bvnNumber by mutableStateOf("")
        private set

    var otp by mutableStateOf("")
        private set

    init {
        viewModelScope.launch(getExceptionHandler(errorProxy)) {
            authResponse = getAuthResponse()
        }
    }

    fun onConsentGranted() {
        _uiState.update { it.copy(currentScreen = BvnConsentScreens.BvnInputScreen) }
    }

    internal fun updateBvnNumber(input: String) {
        if (input.length <= BVN_LENGTH) {
            _uiState.update { it.copy(isBvnValid = input.length == BVN_LENGTH, showError = false) }
            bvnNumber = input
        }
    }

    internal fun updateOtp(input: String) {
        if (input.length <= BVN_OTP_LENGTH) {
            _uiState.update { it.copy(isBvnOtpValid = input.length == BVN_OTP_LENGTH) }
            otp = input
        }
    }

    internal fun updateMode(input: BvnOtpVerificationMode) {
        _uiState.update { it.copy(selectedBvnOtpVerificationMode = input) }
        otp = ""
    }

    internal fun selectContactMethod() {
        _uiState.update {
            it.copy(
                currentScreen = BvnConsentScreens.ChooseOtpDeliveryScreen,
                showError = false,
            )
        }
    }

    internal fun submitUserBvn() {
        if (bvnNumber.length != BVN_LENGTH) {
            Timber.w("State mismatch: bvnNumber.length != BVN_LENGTH")
            return
        }
        if (uiState.value.showLoading) {
            Timber.w("A request is already in progress")
            return
        }

        _uiState.update { it.copy(showLoading = true) }

        viewModelScope.launch(getExceptionHandler(errorProxy)) {
            val authResponse = authResponse ?: getAuthResponse()
            val request = BvnTotpRequest(
                idNumber = bvnNumber,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val response = SmileID.api.requestBvnTotpMode(request = request)
            _uiState.update {
                it.copy(
                    currentScreen = BvnConsentScreens.ChooseOtpDeliveryScreen,
                    showLoading = false,
                    sessionId = response.sessionId,
                    showError = false,
                    bvnVerificationModes = createBvnOtpVerificationModes(response.modes),
                )
            }
        }
    }

    internal fun requestBvnOtp() {
        if (uiState.value.showLoading) {
            Timber.w("A request is already in progress")
            return
        }
        _uiState.update { it.copy(showLoading = true) }
        viewModelScope.launch(getExceptionHandler(errorProxy)) {
            val authResponse = authResponse ?: getAuthResponse()
            val request = BvnTotpModeRequest(
                idNumber = bvnNumber,
                mode = uiState.value.selectedBvnOtpVerificationMode?.otpSentBy ?: "",
                sessionId = uiState.value.sessionId,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val response = SmileID.api.requestBvnOtp(request = request)
            if (response.success) {
                _uiState.update {
                    it.copy(
                        currentScreen = BvnConsentScreens.VerifyOtpScreen,
                        showLoading = false,
                    )
                }
            } else {
                _uiState.update { it.copy(showLoading = false, showError = true) }
            }
        }
    }

    internal fun submitBvnOtp() {
        if (otp.length != BVN_OTP_LENGTH) {
            Timber.w("State mismatch: otp.length != BVN_OTP_LENGTH")
            return
        }
        if (uiState.value.showLoading) {
            Timber.w("A request is already in progress")
            return
        }

        _uiState.update { it.copy(showLoading = true) }

        viewModelScope.launch(getExceptionHandler(errorProxy)) {
            val authResponse = authResponse ?: getAuthResponse()
            val request = SubmitBvnTotpRequest(
                idNumber = bvnNumber,
                otp = otp,
                sessionId = uiState.value.sessionId,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val response = SmileID.api.submitBvnOtp(request = request)
            _uiState.update {
                it.copy(
                    showLoading = false,
                    showSuccess = response.success,
                    showError = !response.success,
                )
            }
        }
    }

    private suspend fun getAuthResponse() = SmileID.api.authenticate(
        AuthenticationRequest(
            userId = userId,
            jobType = JobType.BVN,
        ),
    )
}
