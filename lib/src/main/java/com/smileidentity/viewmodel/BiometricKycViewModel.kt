package com.smileidentity.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobStatusRequest
import com.smileidentity.models.JobType
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.FileType
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class BiometricKycUiState(
    val processingState: ProcessingState? = null,
    @StringRes val errorMessage: Int? = null,
)

class BiometricKycViewModel(
    private val idInfo: IdInfo,
    private val userId: String,
    private val jobId: String,
    private val allowNewEnroll: Boolean,
    private val extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(BiometricKycUiState())
    val uiState = _uiState.asStateFlow()

    private var result: SmileIDResult<BiometricKycResult>? = null
    private var selfieFile: File? = null
    private var livenessFiles: List<File>? = null

    fun onSelfieCaptured(selfieFile: File, livenessFiles: List<File>) {
        this.selfieFile = selfieFile
        this.livenessFiles = livenessFiles
        submitJob(selfieFile, livenessFiles)
    }

    private fun submitJob(selfieFile: File, livenessFiles: List<File>) {
        _uiState.update { it.copy(processingState = ProcessingState.InProgress) }
        val proxy = fun(e: Throwable) {
            Timber.e(e)
            val errorMessage = if (SmileID.allowOfflineMode && isNetworkFailure(e)) {
                R.string.si_offline_message
            } else {
                R.string.si_processing_error_subtitle
            }
            if (!(SmileID.allowOfflineMode && isNetworkFailure(e))) {
                val complete = moveJobToSubmitted(jobId)
                if (!complete) {
                    Timber.w("Failed to move job $jobId to complete")
                    SmileIDCrashReporting.hub.addBreadcrumb(
                        Breadcrumb().apply {
                            category = "Offline Mode"
                            message = "Failed to move job $jobId to complete"
                            level = SentryLevel.INFO
                        },
                    )
                }
            }
            result = SmileIDResult.Error(e)
            _uiState.update {
                it.copy(
                    processingState = ProcessingState.Error,
                    errorMessage = errorMessage,
                )
            }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                jobType = JobType.BiometricKyc,
                userId = userId,
                jobId = jobId,
            )

            if (SmileID.allowOfflineMode) {
                createAuthenticationRequestFile(jobId, authRequest)
            }

            val authResponse = SmileID.api.authenticate(authRequest)

            val prepUploadRequest = PrepUploadRequest(
                partnerParams = authResponse.partnerParams.copy(extras = extraPartnerParams),
                // TODO : Michael will change this to boolean
                allowNewEnroll = allowNewEnroll.toString(),
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            if (SmileID.allowOfflineMode) {
                createPrepUploadFile(jobId, prepUploadRequest)
            }
            val prepUploadResponse = SmileID.api.prepUpload(prepUploadRequest)
            val livenessImagesInfo = livenessFiles.map { it.asLivenessImage() }
            val selfieImageInfo = selfieFile.asSelfieImage()
            val uploadRequest = UploadRequest(
                images = livenessImagesInfo + selfieImageInfo,
                idInfo = idInfo.copy(entered = true),
            )
            SmileID.api.upload(prepUploadResponse.uploadUrl, uploadRequest)
            Timber.d("Upload finished")
            val jobStatusRequest = JobStatusRequest(
                jobId = authResponse.partnerParams.jobId,
                userId = authResponse.partnerParams.userId,
                includeImageLinks = false,
                includeHistory = false,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )

            val jobStatusResponse = SmileID.api.getBiometricKycJobStatus(jobStatusRequest)
            var selfieFileResult = selfieFile
            var livenessFilesResult = livenessFiles
            // if we've gotten this far we move files
            // to complete from pending
            val copySuccess = moveJobToSubmitted(jobId)
            if (copySuccess) {
                selfieFileResult = getFileByType(jobId, FileType.SELFIE) ?: run {
                    Timber.w("Selfie file not found for job ID: $jobId")
                    throw Exception("Selfie file not found for job ID: $jobId")
                }
                livenessFilesResult = getFilesByType(jobId, FileType.LIVENESS)
            } else {
                Timber.w("Failed to move job $jobId to complete")
                SmileIDCrashReporting.hub.addBreadcrumb(
                    Breadcrumb().apply {
                        category = "Offline Mode"
                        message = "Failed to move job $jobId to complete"
                        level = SentryLevel.INFO
                    },
                )
            }
            result = SmileIDResult.Success(
                BiometricKycResult(
                    selfieFileResult,
                    livenessFilesResult,
                    jobStatusResponse,
                ),
            )
            _uiState.update { it.copy(processingState = ProcessingState.Success) }
        }
    }

    fun onRetry() {
        if (selfieFile == null || livenessFiles == null) {
            Timber.w("Unexpected state: Selfie or liveness files are null")
            // Set processing state to null to redirect back to selfie capture
            _uiState.update { it.copy(processingState = null) }
        } else {
            submitJob(selfieFile!!, livenessFiles!!)
        }
    }

    fun onFinished(onResult: SmileIDCallback<BiometricKycResult>) = onResult(result!!)
}
