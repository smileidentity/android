package com.smileidentity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.SmileIDException
import com.smileidentity.models.UploadRequest
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.FileType
import com.smileidentity.util.StringResource
import com.smileidentity.util.createAuthenticationRequestFile
import com.smileidentity.util.createPrepUploadFile
import com.smileidentity.util.createUploadRequestFile
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.getFileByType
import com.smileidentity.util.getFilesByType
import com.smileidentity.util.handleOfflineJobFailure
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
    val errorMessage: StringResource = StringResource.ResId(R.string.si_processing_error_subtitle),
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
            val didMoveToSubmitted = handleOfflineJobFailure(jobId, e)
            if (didMoveToSubmitted) {
                this.selfieFile = getFileByType(jobId, FileType.SELFIE)
                this.livenessFiles = getFilesByType(jobId, FileType.LIVENESS)
            }
            if (SmileID.allowOfflineMode && isNetworkFailure(e)) {
                result = SmileIDResult.Success(
                    BiometricKycResult(
                        selfieFile,
                        livenessFiles,
                        true,
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
            val authRequest = AuthenticationRequest(
                jobType = JobType.BiometricKyc,
                userId = userId,
                jobId = jobId,
            )

            if (SmileID.allowOfflineMode) {
                createAuthenticationRequestFile(jobId, authRequest)
                createPrepUploadFile(
                    jobId,
                    PrepUploadRequest(
                        partnerParams = PartnerParams(
                            jobType = JobType.BiometricKyc,
                            jobId = jobId,
                            userId = userId,
                            extras = extraPartnerParams,
                        ),
                        allowNewEnroll = allowNewEnroll.toString(),
                        timestamp = "",
                        signature = "",
                    ),
                )
                createUploadRequestFile(
                    jobId,
                    UploadRequest(
                        images = livenessFiles.map { it.asLivenessImage() } +
                            selfieFile.asSelfieImage(),
                        idInfo = idInfo.copy(entered = true),
                    ),
                )
            }

            val authResponse = SmileID.api.authenticate(authRequest)

            val prepUploadRequest = PrepUploadRequest(
                partnerParams = authResponse.partnerParams.copy(extras = extraPartnerParams),
                // TODO : Michael will change this to boolean
                allowNewEnroll = allowNewEnroll.toString(),
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )

            val prepUploadResponse = SmileID.api.prepUpload(prepUploadRequest)
            val livenessImagesInfo = livenessFiles.map { it.asLivenessImage() }
            val selfieImageInfo = selfieFile.asSelfieImage()
            val uploadRequest = UploadRequest(
                images = livenessImagesInfo + selfieImageInfo,
                idInfo = idInfo.copy(entered = true),
            )
            SmileID.api.upload(prepUploadResponse.uploadUrl, uploadRequest)
            Timber.d("Upload finished")

            var selfieFileResult = selfieFile
            var livenessFilesResult = livenessFiles
            // if we've gotten this far, we move files to complete from pending
            val copySuccess = moveJobToSubmitted(jobId)
            if (copySuccess) {
                selfieFileResult = getFileByType(jobId, FileType.SELFIE) ?: run {
                    Timber.w("Selfie file not found for job ID: $jobId")
                    throw IllegalStateException("Selfie file not found for job ID: $jobId")
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
                    selfieFile = selfieFileResult,
                    livenessFiles = livenessFilesResult,
                    didSubmitBiometricKycJob = true,
                ),
            )
            _uiState.update {
                it.copy(
                    processingState = ProcessingState.Success,
                    errorMessage = StringResource.ResId(
                        R.string.si_biometric_kyc_processing_success_subtitle,
                    ),
                )
            }
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
