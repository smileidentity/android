package com.smileidentity.viewmodel

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.Document
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobStatusRequest
import com.smileidentity.models.JobType
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.networking.asDocumentImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.createDocumentFile
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.postProcessImage
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class DocumentUiState(
    val frontDocumentImageToConfirm: File? = null,
    val backDocumentImageToConfirm: File? = null,
    val showSelfieCapture: Boolean = false,
    val processingState: ProcessingState? = null,
    @StringRes val errorMessage: Int? = null,
)

/**
 * @param selfieFile The selfie image file to use for authentication. If null, selfie capture will
 * be performed
 */
class DocumentViewModel(
    private val userId: String,
    private val jobId: String,
    private val idType: Document,
    private val idAspectRatio: Float? = idType.aspectRatio,
    private var selfieFile: File? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState = _uiState.asStateFlow()
    var result: SmileIDResult<DocumentVerificationResult>? = null
    private var documentFrontFile: File? = null
    private var documentBackFile: File? = null

    internal fun takeButtonCaptureDocument(
        cameraState: CameraState,
        hasBackSide: Boolean,
    ) {
        viewModelScope.launch {
            try {
                val documentFile =
                    captureDocument(cameraState = cameraState, forBackSide = hasBackSide)
                Timber.v("Capturing document image to $documentFile and is $hasBackSide")
                _uiState.update {
                    if (hasBackSide) {
                        it.copy(backDocumentImageToConfirm = documentFile)
                    } else {
                        it.copy(
                            frontDocumentImageToConfirm = documentFile,
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e("Error capturing document", e)
                _uiState.update { it.copy(errorMessage = R.string.si_doc_v_capture_error_subtitle) }
            }
        }
    }

    /**
     * Captures a document image using the given [cameraState] and returns the processed image as a [Bitmap].
     * If an error occurs during capture or processing, the coroutine will be resumed with an exception.
     * The [documentFrontFile] or [documentBackFile] variable will be updated with the captured image file,
     * and the UI state will be updated accordingly.`
     */
    private suspend fun captureDocument(cameraState: CameraState, forBackSide: Boolean) =
        suspendCoroutine {
            if (forBackSide) {
                documentBackFile = createDocumentFile()
            } else {
                documentFrontFile = createDocumentFile()
            }
            val documentFile = if (forBackSide) documentBackFile!! else documentFrontFile!!
            cameraState.takePicture(documentFile) { result ->
                when (result) {
                    is ImageCaptureResult.Error -> it.resumeWithException(result.throwable)
                    is ImageCaptureResult.Success -> it.resume(
                        postProcessImage(file = documentFile, desiredAspectRatio = idAspectRatio),
                    )
                }
            }
        }

    fun saveFileFromGallerySelection(
        documentFile: File?,
        isBackSide: Boolean = false,
    ) {
        _uiState.update {
            if (isBackSide) {
                it.copy(backDocumentImageToConfirm = documentFile)
            } else {
                it.copy(
                    frontDocumentImageToConfirm = documentFile,
                )
            }
        }
    }

    fun submitJob(documentFrontFile: File, documentBackFile: File? = null): Job {
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
        return viewModelScope.launch(getExceptionHandler(proxy)) {
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
            val frontImageInfo = documentFrontFile.asDocumentImage()
            val backImageInfo = documentBackFile?.asDocumentImage()
            val selfieImageInfo = selfieFile?.asSelfieImage() ?: throw IllegalStateException(
                "Selfie file is null",
            )
            val uploadRequest = UploadRequest(
                images = listOfNotNull(frontImageInfo, backImageInfo, selfieImageInfo),
                idInfo = IdInfo(idType.countryCode, idType.documentType),
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

            val jobStatusResponse = SmileID.api.getDocVJobStatus(jobStatusRequest)
            result = SmileIDResult.Success(
                DocumentVerificationResult(
                    selfieFile = selfieImageInfo.image,
                    documentFrontFile = documentFrontFile,
                    documentBackFile = documentBackFile,
                    jobStatusResponse = jobStatusResponse,
                ),
            )
            _uiState.update { it.copy(processingState = ProcessingState.Success) }
        }
    }

    fun onDocumentConfirmed() {
        _uiState.update { it.copy(showSelfieCapture = true) }
    }

    fun onDocumentRejected(
        isBackSide: Boolean = false,
    ) {
        _uiState.update {
            if (isBackSide) {
                it.copy(backDocumentImageToConfirm = null)
            } else {
                it.copy(
                    frontDocumentImageToConfirm = null,
                )
            }
        }
        if (isBackSide) {
            documentBackFile?.delete()?.also { deleted ->
                if (!deleted) Timber.w("Failed to delete $documentBackFile")
            }
            documentBackFile = null
        } else {
            documentFrontFile?.delete()?.also { deleted ->
                if (!deleted) Timber.w("Failed to delete $documentFrontFile")
            }
            documentFrontFile = null
        }
        result = null
    }

    fun onRetry(hasBackSide: Boolean) {
        // If document files are present, all captures were completed, so we're retrying a network issue
        when {
            hasBackSide -> if (documentFrontFile != null && documentBackFile != null) {
                submitJob(documentFrontFile!!, documentBackFile)
            } else {
                _uiState.update {
                    it.copy(processingState = null)
                }
            }

            else -> {
                if (documentFrontFile != null) {
                    submitJob(documentFrontFile!!)
                } else {
                    _uiState.update {
                        it.copy(processingState = null)
                    }
                }
            }
        }
    }

    fun onFinished(callback: SmileIDCallback<DocumentVerificationResult>) {
        callback(result!!)
    }

    fun onSelfieCaptureError(error: SmileIDResult.Error) {
        Timber.w("Selfie capture error: $error")
        _uiState.update { it.copy(processingState = ProcessingState.Error) }
    }

    fun onSelfieCaptureSuccess(it: SmileIDResult.Success<SmartSelfieResult>) {
        selfieFile = it.data.selfieFile
        submitJob(documentFrontFile!!, documentBackFile)
    }
}
