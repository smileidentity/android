package com.smileidentity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.EnhancedKycRequest
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.results.EnhancedKycResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

internal data class EnhancedKycUiState(
    val showLoading: Boolean = true,
    val showConsent: Boolean = false,
    val processingState: ProcessingState? = null,
    val errorMessage: String? = null,
)

internal class EnhancedKycViewModel(
    private val userId: String,
    private val jobId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EnhancedKycUiState())
    val uiState = _uiState.asStateFlow()

    private lateinit var idInfo: IdInfo
    private var result: SmileIDResult<EnhancedKycResult>? = null

    init {
        // Check whether consent is required (returned in the auth smile response)
        // on error, fall back to showing consent
        val proxy = { e: Throwable ->
            Timber.w(e)
            _uiState.update { it.copy(showLoading = false, showConsent = true) }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                jobType = JobType.EnhancedKyc,
                userId = userId,
                jobId = jobId,
                country = idInfo.country,
                idType = idInfo.idType,
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            if (authResponse.consentInfo?.consentRequired == true) {
                _uiState.update { it.copy(showLoading = false, showConsent = true) }
            } else {
                _uiState.update { it.copy(showLoading = false, showConsent = false) }
            }
        }
    }

    fun onConsentGranted() {
        _uiState.update { it.copy(showConsent = false) }
    }

    fun onIdInfoReceived(idInfo: IdInfo) {
        this.idInfo = idInfo
        doEnhancedKyc()
    }

    private fun doEnhancedKyc() {
        _uiState.update { it.copy(processingState = ProcessingState.InProgress) }
        val proxy = { e: Throwable ->
            result = SmileIDResult.Error(e)
            _uiState.update {
                it.copy(processingState = ProcessingState.Error, errorMessage = e.message)
            }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                jobType = JobType.EnhancedKyc,
                enrollment = false,
                userId = randomUserId(),
                jobId = randomJobId(),
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val enhancedKycRequest = EnhancedKycRequest(
                partnerParams = authResponse.partnerParams,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
                country = idInfo.country,
                idType = idInfo.idType!!,
                idNumber = idInfo.idNumber ?: throw IllegalArgumentException("ID Number required"),
                firstName = idInfo.firstName,
                lastName = idInfo.lastName,
                dob = idInfo.dob,
                bankCode = idInfo.bankCode,
            )
            val response = SmileID.api.doEnhancedKyc(enhancedKycRequest)
            result = SmileIDResult.Success(EnhancedKycResult(enhancedKycRequest, response))
            _uiState.update { it.copy(processingState = ProcessingState.Success) }
        }
    }

    fun onRetry() {
        doEnhancedKyc()
    }

    fun onFinished(callback: SmileIDCallback<EnhancedKycResult>) {
        callback(result!!)
    }
}
