package com.smileidentity.viewmodel

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.annotation.StringRes
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.smileidentity.R
import com.smileidentity.createDocumentFile
import com.smileidentity.models.Document
import com.smileidentity.postProcessImage
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmileIDResult
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
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
    val documentImageToConfirm: File? = null,
    @StringRes val errorMessage: Int? = null,
    val currentBoundingBox: Rect? = null,
    val fullImageWidth: Int = 0,
    val fullImageHeight: Int = 0,
) {
    val isDocumentDetected: Boolean get() = currentBoundingBox != null
}

class DocumentViewModel(
    private val userId: String,
    private val jobId: String,
    private val enforcedIdType: Document? = null,
    private val idAspectRatio: Float? = enforcedIdType?.aspectRatio,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState = _uiState.asStateFlow()
    var result: SmileIDResult<DocumentVerificationResult>? = null
    private var documentFile: File? = null

    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .build(),
    )

    internal fun takeButtonCaptureDocument(
        cameraState: CameraState,
    ) {
        viewModelScope.launch {
            try {
                val documentFile = captureDocument(cameraState)
                Timber.v("Capturing document image to $documentFile")
                _uiState.update { it.copy(documentImageToConfirm = documentFile) }
            } catch (e: Exception) {
                Timber.e("Error capturing document", e)
                _uiState.update { it.copy(errorMessage = R.string.si_doc_v_capture_error_subtitle) }
            }
        }
    }

    /**
     * Captures a document image using the given [cameraState] and returns the processed image as a [Bitmap].
     * If an error occurs during capture or processing, the coroutine will be resumed with an exception.
     * The [documentFile] variable will be updated with the captured image file, and the UI state will be updated accordingly.
     */
    private suspend fun captureDocument(cameraState: CameraState) = suspendCoroutine {
        documentFile = createDocumentFile()
        cameraState.takePicture(documentFile!!) { result ->
            when (result) {
                is ImageCaptureResult.Error -> it.resumeWithException(result.throwable)
                is ImageCaptureResult.Success -> it.resume(
                    postProcessImage(
                        file = documentFile!!,
                    ),
                )
            }
        }
    }

    private fun submitJob(documentFile: File) {
    }

    fun submitJob() {
        submitJob(documentFile = documentFile!!)
    }

    fun onDocumentRejected() {
        _uiState.update {
            it.copy(
                documentImageToConfirm = null,
            )
        }
        documentFile?.delete()?.also { deleted ->
            if (!deleted) Timber.w("Failed to delete $documentFile")
        }
        documentFile = null
        result = null
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun analyzeImage(imageProxy: ImageProxy) {
        val image = imageProxy.image
        if (image == null) {
            Timber.w("DocV Image is null. Aborting image analysis")
            imageProxy.close()
            return
        }
        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        objectDetector.process(inputImage)
            .addOnSuccessListener { detectedObjects ->
                Timber.v("DocV Detected ${detectedObjects.size} objects")
                detectedObjects.forEach { obj ->
                    Timber.v("DocV Detected object: ${obj.trackingId}")
                    Timber.v("DocV Detected object bounding box: ${obj.boundingBox}")
                    _uiState.update {
                        it.copy(
                            currentBoundingBox = obj.boundingBox,
                            fullImageWidth = imageProxy.width,
                            fullImageHeight = imageProxy.height,
                        )
                    }
                    obj.labels.forEach { label ->
                        Timber.v("DocV Detected object label: ${label.text}")
                        Timber.v("DocV Detected object label confidence: ${label.confidence}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Timber.e(e, "DocV Error detecting objects")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
