package com.smileidentity.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.smileidentity.R
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.results.EnhancedDocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
