package com.smileidentity.compose.selfie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.JobType.SmartSelfieAuthentication
import com.smileidentity.models.JobType.SmartSelfieEnrollment
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.SmileIDException
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.models.v2.asNetworkRequest
import com.smileidentity.networking.doSmartSelfieAuthentication
import com.smileidentity.networking.doSmartSelfieEnrollment
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.FileType
import com.smileidentity.util.StringResource
import com.smileidentity.util.createAuthenticationRequestFile
import com.smileidentity.util.createPrepUploadFile
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.getFileByType
import com.smileidentity.util.getFilesByType
import com.smileidentity.util.isNetworkFailure
import com.smileidentity.util.moveJobToSubmitted
import io.sentry.Breadcrumb
import io.sentry.SentryLevel
import java.io.File
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

internal data class SelfieUiState(
    val processingState: ProcessingState? = null,
    val errorMessage: StringResource = StringResource.ResId(R.string.si_processing_error_subtitle),
)

internal class OrchestratedSelfieViewModel(
    private val isEnroll: Boolean,
    private val userId: String,
    private val jobId: String,
    private val allowNewEnroll: Boolean,
    private val metadata: MutableList<Metadatum>,
    private val extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelfieUiState())

    val uiState = _uiState.asStateFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        SelfieUiState(),
    )

    var result: SmileIDResult<SmartSelfieResult>? = null

    fun submitJob(selfieFile: File, livenessFiles: List<File>) {
        _uiState.update { it.copy(processingState = ProcessingState.InProgress) }

        val proxy = fun(e: Throwable) {
            if (SmileID.allowOfflineMode && isNetworkFailure(e)) {
                result = SmileIDResult.Success(
                    SmartSelfieResult(
                        selfieFile = selfieFile,
                        livenessFiles = livenessFiles,
                        apiResponse = null,
                    ),
                )
                _uiState.update {
                    it.copy(
                        processingState = ProcessingState.Success,
                        errorMessage = StringResource.ResId(R.string.si_offline_message),
                    )
                }
            } else {
                val errorMessage: StringResource = when {
                    isNetworkFailure(e) -> StringResource.ResId(R.string.si_no_internet)
                    e is SmileIDException -> StringResource.ResIdFromSmileIDException(e)
                    else -> StringResource.ResId(R.string.si_processing_error_subtitle)
                }
                result = SmileIDResult.Error(e)
                _uiState.update {
                    it.copy(
                        processingState = ProcessingState.Error,
                        errorMessage = errorMessage,
                    )
                }
            }
        }

        viewModelScope.launch(getExceptionHandler(proxy)) {
            if (SmileID.allowOfflineMode) {
                // For the moment, we continue to use the async API endpoints for offline mode
                val jobType = if (isEnroll) SmartSelfieEnrollment else SmartSelfieAuthentication
                val authRequest = AuthenticationRequest(
                    jobType = jobType,
                    enrollment = isEnroll,
                    userId = userId,
                    jobId = jobId,
                )
                createAuthenticationRequestFile(jobId, authRequest)
                createPrepUploadFile(
                    jobId,
                    PrepUploadRequest(
                        partnerParams = PartnerParams(
                            jobType = jobType,
                            jobId = jobId,
                            userId = userId,
                            extras = extraPartnerParams,
                        ),
                        allowNewEnroll = allowNewEnroll.toString(),
                        metadata = metadata,
                        timestamp = "",
                        signature = "",
                    ),
                )
            }

            val apiResponse = if (isEnroll) {
                SmileID.api.doSmartSelfieEnrollment(
                    selfieImage = selfieFile,
                    livenessImages = livenessFiles,
                    userId = userId,
                    partnerParams = extraPartnerParams,
                    allowNewEnroll = allowNewEnroll,
                    metadata = metadata.asNetworkRequest(),
                )
            } else {
                SmileID.api.doSmartSelfieAuthentication(
                    selfieImage = selfieFile,
                    livenessImages = livenessFiles,
                    userId = userId,
                    partnerParams = extraPartnerParams,
                    metadata = metadata.asNetworkRequest(),
                )
            }
            // Move files from unsubmitted to submitted directories
            val copySuccess = moveJobToSubmitted(jobId)
            val (selfieFileResult, livenessFilesResult) = if (copySuccess) {
                val selfieFileResult = getFileByType(jobId, FileType.SELFIE) ?: run {
                    Timber.w("Selfie file not found for job ID: $jobId")
                    throw IllegalStateException("Selfie file not found for job ID: $jobId")
                }
                val livenessFilesResult = getFilesByType(jobId, FileType.LIVENESS)
                selfieFileResult to livenessFilesResult
            } else {
                Timber.w("Failed to move job $jobId to complete")
                SmileIDCrashReporting.hub.addBreadcrumb(
                    Breadcrumb().apply {
                        category = "Offline Mode"
                        message = "Failed to move job $jobId to complete"
                        level = SentryLevel.INFO
                    },
                )
                selfieFile to livenessFiles
            }
            result = SmileIDResult.Success(
                SmartSelfieResult(
                    selfieFile = selfieFileResult,
                    livenessFiles = livenessFilesResult,
                    apiResponse = apiResponse,
                ),
            )
            _uiState.update {
                it.copy(
                    processingState = ProcessingState.Success,
                    errorMessage = StringResource.ResId(
                        R.string.si_smart_selfie_processing_success_subtitle,
                    ),
                )
            }
        }
    }

    fun onFinished(callback: SmileIDCallback<SmartSelfieResult>) {
        callback(result!!)
    }
}
