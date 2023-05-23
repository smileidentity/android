package com.smileidentity.viewmodel

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.annotation.StringRes
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.smileidentity.R
import com.smileidentity.compose.ProcessingState
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
    val allowCapture: Boolean = true,
    val processingState: ProcessingState? = null,
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

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    internal fun takeButtonCaptureDocument(
        cameraState: CameraState,
    ) {
        _uiState.update {
            it.copy(
                allowCapture = false,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            try {
                val documentFile = captureDocument(cameraState)
                Timber.v("Capturing document image to $documentFile")
                _uiState.update {
                    it.copy(
                        documentImageToConfirm = documentFile,
                        errorMessage = null,
                    )
                }
            } catch (e: Exception) {
                Timber.e("Error capturing document", e)
                _uiState.update {
                    it.copy(
                        allowCapture = true,
                        errorMessage = R.string.si_doc_v_capture_error_subtitle,
                    )
                }
            }
        }
    }

    /**
     * Captures a document image using the given [cameraState] and returns the processed image as a
     * [android.graphics.Bitmap]. If an error occurs during capture or processing, the coroutine
     * will be resumed with an exception. The [documentFile] variable will be updated with the
     * captured image file, and the UI state will be updated accordingly.
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
        _uiState.update { it.copy(processingState = ProcessingState.InProgress) }
    }

    fun submitJob() = submitJob(documentFile = documentFile!!)

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

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotEmpty()) {
                    Timber.v("DocV Detected ${visionText.text}")
                }
            }
            .addOnFailureListener { Timber.w(it, "DocV Error detecting text") }
            .continueWithTask {
                if (it.isSuccessful && it.result.text.isNotEmpty()) {
                    return@continueWithTask objectDetector.process(inputImage)
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
                        .addOnFailureListener { Timber.e(it, "DocV Error detecting objects") }
                        .continueWithTask { Tasks.forResult(Unit) }
                } else {
                    Timber.v("Didn't find any text, not performing object recognition")
                    return@continueWithTask Tasks.forResult(Unit)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
