package com.smileidentity.sample.viewmodel

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
import com.smileidentity.util.randomUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EnhancedKycUiState(
    val processingState: ProcessingState? = null,
    val errorMessage: String? = null,
)

class EnhancedKycViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EnhancedKycUiState())
    val uiState = _uiState.asStateFlow()

    private lateinit var idInfo: IdInfo
    private var result: SmileIDResult<EnhancedKycResult>? = null

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
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val enhancedKycRequest = EnhancedKycRequest(
                partnerParams = authResponse.partnerParams,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
                country = idInfo.country,
                idType = idInfo.idType,
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
