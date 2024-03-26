package com.smileidentity.viewmodel.document

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.networking.asDocumentBackImage
import com.smileidentity.networking.asDocumentFrontImage
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.EnhancedDocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.FileType
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

internal data class OrchestratedDocumentUiState(
    val currentStep: DocumentCaptureFlow = DocumentCaptureFlow.FrontDocumentCapture,
    @StringRes val errorMessage: Int? = null,
)

/**
 * @param selfieFile The selfie image file to use for authentication. If null, selfie capture will
 * be performed
 */
internal abstract class OrchestratedDocumentViewModel<T : Parcelable>(
    private val jobType: JobType,
    private val userId: String,
    protected val jobId: String,
    private val allowNewEnroll: Boolean,
    private val countryCode: String,
    private val documentType: String? = null,
    private val captureBothSides: Boolean,
    protected var selfieFile: File? = null,
    private var extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrchestratedDocumentUiState())
    val uiState = _uiState.asStateFlow()
    var result: SmileIDResult<T> = SmileIDResult.Error(
        IllegalStateException("Document Capture incomplete"),
    )
    protected var documentFrontFile: File? = null
    protected var documentBackFile: File? = null
    protected var livenessFiles: List<File>? = null
    private var stepToRetry: DocumentCaptureFlow? = null

    fun onDocumentFrontCaptureSuccess(documentImageFile: File) {
        documentFrontFile = documentImageFile
        if (captureBothSides) {
            _uiState.update {
                it.copy(currentStep = DocumentCaptureFlow.BackDocumentCapture, errorMessage = null)
            }
        } else if (selfieFile == null) {
            _uiState.update {
                it.copy(currentStep = DocumentCaptureFlow.SelfieCapture, errorMessage = null)
            }
        } else {
            submitJob()
        }
    }

    fun onDocumentBackSkip() {
        if (selfieFile == null) {
            _uiState.update {
                it.copy(currentStep = DocumentCaptureFlow.SelfieCapture, errorMessage = null)
            }
        } else {
            submitJob()
        }
    }

    fun onDocumentBackCaptureSuccess(documentImageFile: File) {
        documentBackFile = documentImageFile
        if (selfieFile == null) {
            _uiState.update {
                it.copy(currentStep = DocumentCaptureFlow.SelfieCapture, errorMessage = null)
            }
        } else {
            submitJob()
        }
    }

    fun onSelfieCaptureSuccess(it: SmileIDResult.Success<SmartSelfieResult>) {
        selfieFile = it.data.selfieFile
        livenessFiles = it.data.livenessFiles
        submitJob()
    }

    abstract fun saveResult(
        selfieImage: File,
        documentFrontFile: File,
        documentBackFile: File?,
        livenessFiles: List<File>?,
        didSubmitJob: Boolean,
    )

    private fun submitJob() {
        val documentFrontFile = documentFrontFile
            ?: throw IllegalStateException("documentFrontFile is null")
        _uiState.update {
            it.copy(currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.InProgress))
        }

        viewModelScope.launch(getExceptionHandler(::onError)) {
            val authRequest = AuthenticationRequest(
                jobType = jobType,
                enrollment = false,
                userId = userId,
                jobId = jobId,
            )
            val frontImageInfo = documentFrontFile.asDocumentFrontImage()
            val backImageInfo = documentBackFile?.asDocumentBackImage()
            val selfieImageInfo = selfieFile?.asSelfieImage() ?: throw IllegalStateException(
                "Selfie file is null",
            )
            // Liveness files will be null when the partner bypasses our Selfie capture with a file
            val livenessImageInfo = livenessFiles?.map { it.asLivenessImage() } ?: emptyList()
            val uploadRequest = UploadRequest(
                images = listOfNotNull(
                    frontImageInfo,
                    backImageInfo,
                    selfieImageInfo,
                ) + livenessImageInfo,
                idInfo = IdInfo(countryCode, documentType),
            )

            if (SmileID.allowOfflineMode) {
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
                        timestamp = "",
                        signature = "",
                    ),
                )
                createUploadRequestFile(
                    jobId,
                    uploadRequest,
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
            SmileID.api.upload(prepUploadResponse.uploadUrl, uploadRequest)
            Timber.d("Upload finished")
        }
    }

    fun sendResult(
        documentFrontFile: File,
        documentBackFile: File? = null,
        livenessFiles: List<File>? = null,
    ) {
        var selfieFileResult: File = selfieFile ?: run {
            Timber.w("Selfie file not found for job ID: $jobId")
            throw Exception("Selfie file not found for job ID: $jobId")
        }
        var livenessFilesResult = livenessFiles
        var documentFrontFileResult = documentFrontFile
        var documentBackFileResult = documentBackFile
        val copySuccess = moveJobToSubmitted(jobId)
        if (copySuccess) {
            selfieFileResult = getFileByType(jobId, FileType.SELFIE) ?: run {
                Timber.w("Selfie file not found for job ID: $jobId")
                throw IllegalStateException("Selfie file not found for job ID: $jobId")
            }
            livenessFilesResult = getFilesByType(jobId, FileType.LIVENESS)
            documentFrontFileResult = getFileByType(jobId, FileType.DOCUMENT_FRONT) ?: run {
                Timber.w("Document front file not found for job ID: $jobId")
                throw IllegalStateException("Document front found for job ID: $jobId")
            }
            documentBackFileResult = getFileByType(jobId, FileType.DOCUMENT_BACK)
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

        saveResult(
            selfieImage = selfieFileResult,
            documentFrontFile = documentFrontFileResult,
            documentBackFile = documentBackFileResult,
            livenessFilesResult,
            didSubmitJob = true,
        )

        _uiState.update {
            it.copy(currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Success))
        }
    }

    /**
     * Trigger the display of the Error dialog
     */
    fun onError(throwable: Throwable) {
        handleOfflineJobFailure(jobId, throwable)
        stepToRetry = uiState.value.currentStep
        _uiState.update {
            it.copy(
                currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Error),
                errorMessage = R.string.si_processing_error_subtitle,
            )
        }
        if (SmileID.allowOfflineMode && isNetworkFailure(throwable)) {
            saveResult(
                selfieImage = selfieFile ?: throw IllegalStateException("Selfie file is null"),
                documentFrontFile = documentFrontFile ?: throw IllegalStateException(
                    "Document front file is null",
                ),
                documentBackFile = documentBackFile,
                livenessFiles,
                didSubmitJob = false,
            )
            _uiState.update {
                it.copy(currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Success))
            }
        } else {
            result = SmileIDResult.Error(throwable)
            _uiState.update {
                it.copy(
                    currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Error),
                    errorMessage = R.string.si_processing_error_subtitle,
                )
            }
        }
    }

    /**
     * If stepToRetry is ProcessingScreen, we're retrying a network issue, so we need to kick off
     * the resubmission manually. Otherwise, we're retrying a capture error, so we just need to
     * reset the UI state
     */
    fun onRetry() {
        // The step to retry is the one that failed, which should have been saved in onError.
        // onError sets the current step to ProcessingScreen Error.
        val step = stepToRetry
        stepToRetry = null
        step?.let { stepToRetry ->
            _uiState.update { it.copy(currentStep = stepToRetry, errorMessage = null) }
            if (stepToRetry is DocumentCaptureFlow.ProcessingScreen) {
                submitJob()
            }
        }
    }

    fun onFinished(callback: SmileIDCallback<T>) = callback(result)
}

internal class DocumentVerificationViewModel(
    jobType: JobType = JobType.DocumentVerification,
    userId: String,
    jobId: String,
    allowNewEnroll: Boolean,
    countryCode: String,
    documentType: String? = null,
    captureBothSides: Boolean,
    selfieFile: File? = null,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
) : OrchestratedDocumentViewModel<DocumentVerificationResult>(
    jobType = jobType,
    userId = userId,
    jobId = jobId,
    allowNewEnroll = allowNewEnroll,
    countryCode = countryCode,
    documentType = documentType,
    captureBothSides = captureBothSides,
    selfieFile = selfieFile,
    extraPartnerParams = extraPartnerParams,
) {

    override fun saveResult(
        selfieImage: File,
        documentFrontFile: File,
        documentBackFile: File?,
        livenessFiles: List<File>?,
        didSubmitJob: Boolean,
    ) {
        result = SmileIDResult.Success(
            DocumentVerificationResult(
                selfieFile = selfieImage,
                documentFrontFile = documentFrontFile,
                documentBackFile = documentBackFile,
                didSubmitDocumentVerificationJob = didSubmitJob,
            ),
        )
    }
}

internal class EnhancedDocumentVerificationViewModel(
    jobType: JobType = JobType.EnhancedDocumentVerification,
    userId: String,
    jobId: String,
    allowNewEnroll: Boolean,
    countryCode: String,
    documentType: String? = null,
    captureBothSides: Boolean,
    selfieFile: File? = null,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
) : OrchestratedDocumentViewModel<EnhancedDocumentVerificationResult>(
    jobType = jobType,
    userId = userId,
    jobId = jobId,
    allowNewEnroll = allowNewEnroll,
    countryCode = countryCode,
    documentType = documentType,
    captureBothSides = captureBothSides,
    selfieFile = selfieFile,
    extraPartnerParams = extraPartnerParams,
) {

    override fun saveResult(
        selfieImage: File,
        documentFrontFile: File,
        documentBackFile: File?,
        livenessFiles: List<File>?,
        didSubmitJob: Boolean,
    ) {
        result = SmileIDResult.Success(
            EnhancedDocumentVerificationResult(
                selfieImage,
                documentFrontFile,
                livenessFiles,
                documentBackFile,
                didSubmitEnhancedDocVJob = didSubmitJob,
            ),
        )
    }
}
