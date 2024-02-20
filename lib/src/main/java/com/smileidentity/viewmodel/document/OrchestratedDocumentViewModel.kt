package com.smileidentity.viewmodel.document

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobStatusRequest
import com.smileidentity.models.JobType
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
import com.smileidentity.util.createPreUploadFile
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.getFilesByType
import com.smileidentity.util.isNetworkFailure
import com.smileidentity.util.moveJobToComplete
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
    private var documentFrontFile: File? = null
    private var documentBackFile: File? = null
    private var livenessFiles: List<File>? = null
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

    abstract fun getJobStatus(
        jobStatusRequest: JobStatusRequest,
        selfieImage: File,
        documentFrontFile: File,
        documentBackFile: File?,
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
                createPreUploadFile(jobId, prepUploadRequest)
            }
            val prepUploadResponse = SmileID.api.prepUpload(prepUploadRequest)
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

            getJobStatus(
                jobStatusRequest = jobStatusRequest,
                selfieImage = selfieImageInfo.image,
                documentFrontFile = documentFrontFile,
                documentBackFile = documentBackFile,
            )

            _uiState.update {
                it.copy(currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Success))
            }
        }
    }

    /**
     * Trigger the display of the Error dialog
     */
    fun onError(throwable: Throwable) {
        val errorMessage = if (SmileID.allowOfflineMode && isNetworkFailure(throwable)) {
            R.string.si_offline_message
        } else {
            R.string.si_processing_error_subtitle
        }
        if (!(SmileID.allowOfflineMode && isNetworkFailure(throwable))) {
            moveJobToComplete(jobId)
        }
        stepToRetry = uiState.value.currentStep
        _uiState.update {
            it.copy(
                currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Error),
                errorMessage = errorMessage,
            )
        }
        Timber.w(throwable, "Error in $stepToRetry")
        result = SmileIDResult.Error(throwable)
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

    override fun getJobStatus(
        jobStatusRequest: JobStatusRequest,
        selfieImage: File,
        documentFrontFile: File,
        documentBackFile: File?,
    ) {
        viewModelScope.launch {
            val jobStatusResponse =
                SmileID.api.getDocumentVerificationJobStatus(jobStatusRequest)
            var selfieFileResult = selfieImage
            var documentFrontFileResult = documentFrontFile
            var documentBackFileResult = documentBackFile
            // if we've gotten this far we move files
            // to complete from pending
            val copySuccess = moveJobToComplete(jobId)
            if (copySuccess) {
                selfieFileResult = getFilesByType(jobId, FileType.SELFIE).first()
                documentFrontFileResult = getFilesByType(jobId, FileType.DOCUMENT).first()
                documentBackFileResult = getFilesByType(jobId, FileType.DOCUMENT).last()
            }
            result = SmileIDResult.Success(
                DocumentVerificationResult(
                    selfieFile = selfieFileResult,
                    documentFrontFile = documentFrontFileResult,
                    documentBackFile = documentBackFileResult,
                    jobStatusResponse = jobStatusResponse,
                ),
            )
        }
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

    override fun getJobStatus(
        jobStatusRequest: JobStatusRequest,
        selfieImage: File,
        documentFrontFile: File,
        documentBackFile: File?,
    ) {
        viewModelScope.launch {
            val jobStatusResponse = SmileID.api.getEnhancedDocumentVerificationJobStatus(
                jobStatusRequest,
            )
            var selfieFileResult = selfieImage
            var documentFrontFileResult = documentFrontFile
            var documentBackFileResult = documentBackFile
            // if we've gotten this far we move files
            // to complete from pending
            val copySuccess = moveJobToComplete(jobId)
            if (copySuccess) {
                selfieFileResult = getFilesByType(jobId, FileType.SELFIE).first()
                documentFrontFileResult = getFilesByType(jobId, FileType.DOCUMENT).first()
                documentBackFileResult = getFilesByType(jobId, FileType.DOCUMENT).last()
            }
            result = SmileIDResult.Success(
                EnhancedDocumentVerificationResult(
                    selfieFile = selfieFileResult,
                    documentFrontFile = documentFrontFileResult,
                    documentBackFile = documentBackFileResult,
                    jobStatusResponse = jobStatusResponse,
                ),
            )
        }
    }
}
