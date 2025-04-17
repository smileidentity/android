package com.smileidentity.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.ConsentInformation
import com.smileidentity.models.EnhancedKycRequest
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.models.v2.metadata.MetadataKey
import com.smileidentity.models.v2.metadata.MetadataManager
import com.smileidentity.models.v2.metadata.MetadataProvider
import com.smileidentity.models.v2.metadata.NetworkMetadataProvider
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

data class EnhancedKycUiState(
    val processingState: ProcessingState? = null,
    val errorMessage: String? = null,
)

class EnhancedKycViewModel : ViewModel() {
    init {
        (
            MetadataManager.providers[MetadataProvider.MetadataProviderType.Network]
                as? NetworkMetadataProvider
            )?.startMonitoring()
    }

    private val _uiState = MutableStateFlow(EnhancedKycUiState())
    val uiState = _uiState.asStateFlow()

    private lateinit var idInfo: IdInfo
    private lateinit var consentInformation: ConsentInformation
    private var result: SmileIDResult<EnhancedKycResult>? = null
    private var networkRetries = 0

    fun onIdInfoReceived(idInfo: IdInfo, consentInformation: ConsentInformation) {
        this.idInfo = idInfo
        this.consentInformation = consentInformation
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
            val metadata = MetadataManager.collectAllMetadata()
            // We can stop monitoring the network traffic after we have collected the metadata
            (
                MetadataManager.providers[MetadataProvider.MetadataProviderType.Network]
                    as? NetworkMetadataProvider
                )?.stopMonitoring()
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
                consentInformation = consentInformation,
                metadata = metadata,
            )
            val response = SmileID.api.doEnhancedKyc(enhancedKycRequest)
            networkRetries = 0
            MetadataManager.removeMetadata(MetadataKey.NetworkRetries)
            result = SmileIDResult.Success(EnhancedKycResult(enhancedKycRequest, response))
            _uiState.update { it.copy(processingState = ProcessingState.Success) }
        }
    }

    fun onRetry() {
        networkRetries++
        MetadataManager.addMetadata(MetadataKey.NetworkRetries, networkRetries.toString())
        doEnhancedKyc()
    }

    fun onFinished(callback: SmileIDCallback<EnhancedKycResult>) {
        callback(result!!)
    }
}
