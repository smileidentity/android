package com.smileidentity.viewmodel

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.ProcessingState
import com.smileidentity.createDocumentFile
import com.smileidentity.getExceptionHandler
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.CaptureMode
import com.smileidentity.models.DocVJobStatusResponse
import com.smileidentity.models.Document
import com.smileidentity.models.JobStatusRequest
import com.smileidentity.models.JobType
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.networking.asDocumentImage
import com.smileidentity.postProcessImage
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds

data class DocumentUiState(
    val frontDocumentImageToConfirm: File? = null,
    val backDocumentImageToConfirm: File? = null,
    val processingState: ProcessingState? = null,
    @StringRes val errorMessage: Int? = null,
)

class DocumentViewModel(
    private val userId: String,
    private val jobId: String,
    private val idType: Document,
    private val idAspectRatio: Float? = idType.aspectRatio,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState = _uiState.asStateFlow()
    var result: SmileIDResult<DocumentVerificationResult>? = null
    private var documentFrontFile: File? = null
    private var documentBackFile: File? = null
    private var documentFrontCaptureMode: CaptureMode? = null
    private var documentBackCaptureMode: CaptureMode? = null

    internal fun takeButtonCaptureDocument(
        cameraState: CameraState,
        hasBackSide: Boolean,
    ) {
        viewModelScope.launch {
            try {
                val documentFile = captureDocument(
                    cameraState = cameraState,
                    forBackSide = hasBackSide,
                )
                Timber.v("Capturing document image to $documentFile and is $hasBackSide")
                if (hasBackSide) {
                    _uiState.update { it.copy(backDocumentImageToConfirm = documentFile) }
                } else {
                    _uiState.update { it.copy(frontDocumentImageToConfirm = documentFile) }
                }
            } catch (e: Exception) {
                Timber.e("Error capturing document", e)
                _uiState.update { it.copy(errorMessage = R.string.si_doc_v_capture_error_subtitle) }
            }
        }
    }

    /**
     * Captures a document image using the given [cameraState] and returns the processed image as a
     * [Bitmap]. If an error occurs during capture or processing, the coroutine will be resumed with
     * an exception. The [documentFrontFile] or [documentBackFile] variable will be updated with the
     * captured image file, and the UI state will be updated accordingly.
     */
    private suspend fun captureDocument(cameraState: CameraState, forBackSide: Boolean) =
        suspendCoroutine {
            if (forBackSide) {
                documentBackFile = createDocumentFile()
                documentBackCaptureMode = CaptureMode.Capture
            } else {
                documentFrontFile = createDocumentFile()
                documentFrontCaptureMode = CaptureMode.Capture
            }
            val documentFile = if (forBackSide) documentBackFile!! else documentFrontFile!!
            cameraState.takePicture(documentFile) { result ->
                when (result) {
                    is ImageCaptureResult.Error -> it.resumeWithException(result.throwable)
                    is ImageCaptureResult.Success -> it.resume(postProcessImage(documentFile))
                }
            }
        }

    fun saveFileFromGallerySelection(documentFile: File?, isBackSide: Boolean = false) {
        if (isBackSide) {
            documentBackCaptureMode = CaptureMode.Upload
            _uiState.update { it.copy(backDocumentImageToConfirm = documentFile) }
        } else {
            documentFrontCaptureMode = CaptureMode.Upload
            _uiState.update { it.copy(frontDocumentImageToConfirm = documentFile) }
        }
    }

    private fun submitJob(documentFrontFile: File, documentBackFile: File? = null) {
        _uiState.update { it.copy(processingState = ProcessingState.InProgress) }
        val proxy = { e: Throwable ->
            result = SmileIDResult.Error(e)
            _uiState.update {
                it.copy(
                    processingState = ProcessingState.Error,
                    errorMessage = R.string.si_processing_error_subtitle,
                )
            }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                jobType = JobType.DocumentVerification,
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
            val frontImageInfo =
                documentFrontFile.asDocumentImage(unwrapCaptureMode(documentFrontCaptureMode))
            val backImageInfo =
                documentBackFile?.asDocumentImage(unwrapCaptureMode(documentBackCaptureMode))
            val uploadRequest = UploadRequest(listOfNotNull(frontImageInfo, backImageInfo))
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

            lateinit var jobStatusResponse: DocVJobStatusResponse
            val jobStatusPollDelay = 1.seconds
            for (i in 1..10) {
                Timber.v("Job Status poll attempt #$i in $jobStatusPollDelay")
                delay(jobStatusPollDelay)
                jobStatusResponse = SmileID.api.getDocVJobStatus(jobStatusRequest)
                Timber.v("Job Status Response: $jobStatusResponse")
                if (jobStatusResponse.jobComplete) {
                    break
                }
            }
            result = SmileIDResult.Success(
                DocumentVerificationResult(
                    documentFrontFile = documentFrontFile,
                    documentBackFile = documentBackFile,
                    jobStatusResponse = jobStatusResponse,
                ),
            )
            _uiState.update { it.copy(processingState = ProcessingState.Success) }
        }
    }

    fun onDocumentRejected(isBackSide: Boolean = false) {
        if (isBackSide) {
            documentBackFile?.delete()?.also { deleted ->
                if (!deleted) Timber.w("Failed to delete $documentBackFile")
            }
            documentBackFile = null
            documentBackCaptureMode = null
            _uiState.update { it.copy(backDocumentImageToConfirm = null) }
        } else {
            documentFrontFile?.delete()?.also { deleted ->
                if (!deleted) Timber.w("Failed to delete $documentFrontFile")
            }
            documentFrontFile = null
            documentFrontCaptureMode = null
            _uiState.update { it.copy(frontDocumentImageToConfirm = null) }
        }
        result = null
    }

    fun onRetry(hasBackSide: Boolean) {
        // If document files are present, all captures were completed, so we're retrying a network
        // issue
        when {
            hasBackSide -> if (documentFrontFile != null && documentBackFile != null) {
                submitJob(documentFrontFile!!, documentBackFile)
            } else {
                _uiState.update { it.copy(processingState = null) }
            }

            else -> {
                if (documentFrontFile != null) {
                    submitJob(documentFrontFile!!)
                } else {
                    _uiState.update { it.copy(processingState = null) }
                }
            }
        }
    }

    fun submitDocVJob(frontDocumentFile: File, backDocumentFile: File?) {
        submitJob(documentFrontFile = frontDocumentFile, documentBackFile = backDocumentFile)
    }

    fun onFinished(callback: SmileIDCallback<DocumentVerificationResult>) {
        callback(result!!)
    }

    private fun unwrapCaptureMode(captureMode: CaptureMode?): CaptureMode {
        if (captureMode == null) {
            Timber.w("Capture mode is null when unwrapping. Defaulting to Capture")
        }
        return captureMode ?: CaptureMode.Capture
    }
}
