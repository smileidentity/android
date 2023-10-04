package com.smileidentity.viewmodel

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
import com.smileidentity.models.JobType.EnhancedDocumentVerification
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.networking.asDocumentBackImage
import com.smileidentity.networking.asDocumentFrontImage
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.results.EnhancedDocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.getExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

internal data class OrchestratedEnhancedDocVUiState(
    val currentStep: DocumentCaptureFlow = DocumentCaptureFlow.SelfieCapture,
    @StringRes val errorMessage: Int? = null,
)

internal class OrchestratedEnhancedDocVViewModel(
    private val userId: String,
    private val jobId: String,
    private val countryCode: String,
    private val documentType: String? = null,
    private val captureBothSides: Boolean,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrchestratedEnhancedDocVUiState())
    val uiState = _uiState.asStateFlow()
    var result: SmileIDResult<EnhancedDocumentVerificationResult> = SmileIDResult.Error(
        IllegalStateException("Document Capture incomplete"),
    )
    private var documentFrontFile: File? = null
    private var documentBackFile: File? = null
    private var selfieFile: File? = null
    private var livenessFiles: List<File>? = null
    private var stepToRetry: DocumentCaptureFlow? = null

    fun onSelfieCaptureSuccess(it: SmileIDResult.Success<SmartSelfieResult>) {
        selfieFile = it.data.selfieFile
        livenessFiles = it.data.livenessFiles
        _uiState.update {
            it.copy(currentStep = DocumentCaptureFlow.FrontDocumentCapture, errorMessage = null)
        }
    }

    fun onDocumentFrontCaptureSuccess(documentImageFile: File) {
        documentFrontFile = documentImageFile
        if (captureBothSides) {
            _uiState.update {
                it.copy(currentStep = DocumentCaptureFlow.BackDocumentCapture, errorMessage = null)
            }
        } else {
            submitJob()
        }
    }

    fun onDocumentBackSkip() {
        submitJob()
    }

    fun onDocumentBackCaptureSuccess(documentImageFile: File) {
        documentBackFile = documentImageFile
        submitJob()
    }

    private fun submitJob() {
        val documentFrontFile = documentFrontFile
            ?: throw IllegalStateException("documentFrontFile is null")
        _uiState.update {
            it.copy(currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.InProgress))
        }
        val proxy = { e: Throwable ->
            result = SmileIDResult.Error(e)
            _uiState.update {
                it.copy(
                    currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Error),
                    errorMessage = R.string.si_processing_error_subtitle,
                )
            }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                jobType = EnhancedDocumentVerification,
                enrollment = false,
                userId = userId,
                jobId = jobId,
            )

            val authResponse = SmileID.api.authenticate(authRequest)

            val prepUploadRequest = PrepUploadRequest(
                callbackUrl = "",
                partnerParams = authResponse.partnerParams,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
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

            val jobStatusResponse = SmileID.api.getEnhancedDocumentVerificationJobStatus(
                jobStatusRequest,
            )
            result = SmileIDResult.Success(
                EnhancedDocumentVerificationResult(jobStatusResponse = jobStatusResponse),
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
        stepToRetry = uiState.value.currentStep
        _uiState.update {
            it.copy(
                currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Error),
                errorMessage = R.string.si_processing_error_subtitle,
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

    fun onFinished(callback: SmileIDCallback<EnhancedDocumentVerificationResult>) = callback(result)
}
