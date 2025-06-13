package com.smileidentity.compose.document.composable

import android.graphics.ImageFormat.YUV_420_888
import android.graphics.Point
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.util.calculateLuminance
import com.smileidentity.util.detectBlur
import com.smileidentity.util.detectGlare
import kotlin.math.abs
import kotlin.math.atan2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

/**
 * Enhanced identity document analyzer that specifically detects ID cards, passports,
 * driving licenses, and other identity documents while rejecting generic objects.
 */
class IdentityDocumentAnalyzer(
    private val luminanceThreshold: Int = 50,
    private val glareBaseThreshold: Int = 230,
    private val glareBaseGlareRatio: Double = 0.05,
    private val blurThreshold: Double = 2.5,
    private val onResult: (
        needsMoreLighting: Boolean,
        detectedDocument: Boolean,
        isDocumentGlared: Boolean,
        isDocumentBlurry: Boolean,
        isDocumentTilted: Boolean,
    ) -> Unit,
    private val onError: (Throwable) -> Unit,
) : ImageAnalysis.Analyzer {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val objectDetector: ObjectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableClassification()
            .build()
        ObjectDetection.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val startTime = System.currentTimeMillis()

        // YUV_420_888 is the format produced by CameraX and needed for Luminance calculation
        check(imageProxy.format == YUV_420_888) {
            Timber.d("IdentityDocumentAnalyzer Unsupported format: ${imageProxy.format}")
            SmileIDCrashReporting.hub.addBreadcrumb("Unsupported format: ${imageProxy.format}")
            imageProxy.close()
            onError(Throwable("Unsupported format: ${imageProxy.format}"))
            return
        }
        val image = imageProxy.image

        // Do a luminance check on the surface first
        // the luminanceThreshold can be adjusted on the constructor
        val luminance = calculateLuminance(imageProxy)
        if (luminance < luminanceThreshold) {
            Timber.d("IdentityDocumentAnalyzer check Low luminance detected")
            imageProxy.close()
            onError(Throwable("Move to a well lit room"))
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(image!!, rotation)

        // Create a separate variable to close later
        val proxyToClose = imageProxy

        objectDetector.process(inputImage)
            .addOnSuccessListener { detectedObjects ->
                Timber.d("IdentityDocumentAnalyzer inside object detector")
                for (detectedObject in detectedObjects) {
                    val boundingBox = detectedObject.boundingBox
                    val trackingId = detectedObject.trackingId
                    for (label in detectedObject.labels) {
                        val confidence = label.confidence

                        // use classifier here

                        Timber.d(
                            "IdentityDocumentAnalyzer bbox $boundingBox trackingID $trackingId " +
                                "label ${label.text} confidence $confidence",
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                Timber.d("IdentityDocumentAnalyzer Object Detection Failed: $e")
                SmileIDCrashReporting.hub.addBreadcrumb("Object Detection Failed: $e")
                onError(Throwable("Object Detection Failed: $e"))
            }
            .addOnCompleteListener { proxyToClose.close() }

        // Do a glare check on the surface first (can be adjusted)
        val isGlareDetected = detectGlare(
            imageProxy = imageProxy,
            glareBaseThreshold = glareBaseThreshold,
            glareBaseGlareRatio = glareBaseGlareRatio,
        )

        // Do a blur check on the surface first (can be adjusted)
        val isBlurDetected = detectBlur(
            imageProxy = imageProxy,
            blurThreshold = blurThreshold,
        )

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                for (block in visionText.textBlocks) {
                    val angle = block.cornerPoints?.let { calculateTextAngle(it) }
                    if (abs(angle ?: 0f) > 5f) {
                        // Text is tilted
                        Timber.d(
                            "IdentityDocumentAnalyzer Is tilted: ${(abs(angle ?: 0f) > 5f)}" +
                                " at $angle",
                        )
                    }
                }
            }
    }

    private fun calculateTextAngle(corners: Array<Point>): Float {
        val topLeft = corners[0]
        val topRight = corners[1]
        val deltaX = topRight.x - topLeft.x
        val deltaY = topRight.y - topLeft.y
        return Math.toDegrees(atan2(deltaY.toDouble(), deltaX.toDouble())).toFloat()
    }
}
