package com.smileidentity.compose.document.composable

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.ImageFormat.YUV_420_888
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.util.calculateLuminance
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.pow
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.imgproc.Imgproc
import timber.log.Timber

/**
 * Enhanced identity document analyzer that specifically detects ID cards, passports,
 * driving licenses, and other identity documents while rejecting generic objects.
 */
class OpenCVIdentityDocumentAnalyzer(
    private val luminanceThreshold: Int = 50,
    private val glareBaseThreshold: Int = 240,
    private val glareBaseGlareRatio: Double = 0.05,
    private val blurThreshold: Double = 100.0,
    private val tiltAngleThreshold: Double = 5.0,
    private val onResult: (
        needsMoreLighting: Boolean,
        detectedDocument: Boolean,
        isDocumentGlared: Boolean,
        isDocumentBlurry: Boolean,
        isDocumentTilted: Boolean,
        isDocumentCentered: Boolean,
        detectedAspectRatio: Float,
    ) -> Unit,
    private val onError: (Throwable) -> Unit,
) : ImageAnalysis.Analyzer {

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
            Timber.d("OpenCVIdentityDocumentAnalyzer Unsupported format: ${imageProxy.format}")
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
            Timber.d("OpenCVIdentityDocumentAnalyzer check Low luminance detected")
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
                if (detectedObjects.isNotEmpty()) {
                    val blurResult = detectBlur(
                        bitmap = imageProxyToBitmap(imageProxy),
                        threshold = blurThreshold,
                    )
                    if (blurResult.isBlurry) {
                        Timber.d(
                            "OpenCVIdentityDocumentAnalyzer Image is blurry. Blur value: " +
                                "${blurResult.blurValue}",
                        )
                    } else {
                        Timber.d(
                            "OpenCVIdentityDocumentAnalyzer Image is sharp. Blur value: " +
                                "${blurResult.blurValue}",
                        )
                    }

                    val glareResult = detectGlare(
                        bitmap = imageProxyToBitmap(imageProxy = imageProxy),
                        pixelThreshold = glareBaseThreshold,
                        glareRatioThreshold = glareBaseGlareRatio,
                    )
                    if (glareResult.hasGlare) {
                        Timber.d(
                            "OpenCVIdentityDocumentAnalyzer Image is Glared. Glare value: " +
                                "${glareResult.glareRatio}",
                        )
                    } else {
                        Timber.d(
                            "OpenCVIdentityDocumentAnalyzer Image is sharp. Glare value: " +
                                "${glareResult.glareRatio}",
                        )
                    }

                    val tiltResult = detectTilt(
                        bitmap = imageProxyToBitmap(imageProxy = imageProxy),
                        angleThreshold = tiltAngleThreshold,
                    )
                    if (tiltResult.isTilted) {
                        Timber.d(
                            "OpenCVIdentityDocumentAnalyzer Image is tilted by " +
                                "${tiltResult.tiltAngle} degrees",
                        )
                    } else {
                        Timber.d(
                            "OpenCVIdentityDocumentAnalyzer Image is properly aligned. " +
                                "Tilt angle: ${tiltResult.tiltAngle}",
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                Timber.d("OpenCVIdentityDocumentAnalyzer Object Detection Failed: $e")
                SmileIDCrashReporting.hub.addBreadcrumb("Object Detection Failed: $e")
                onError(Throwable("Object Detection Failed: $e"))
            }
            .addOnCompleteListener { proxyToClose.close() }
    }

    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val imageBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    /**
     * Detects blur in a Bitmap using the Laplacian variance method.
     *
     * @param bitmap The image to analyze.
     * @param threshold Optional: minimum variance considered not blurry.
     * @return BlurResult containing the variance and whether it is blurry.
     */
    fun detectBlur(bitmap: Bitmap, threshold: Double = 100.0): BlurResult {
        val sourceMat = Mat()
        val grayMat = Mat()
        val laplacianMat = Mat()

        try {
            Utils.bitmapToMat(bitmap, sourceMat)

            Imgproc.cvtColor(sourceMat, grayMat, Imgproc.COLOR_BGR2GRAY)

            Imgproc.Laplacian(grayMat, laplacianMat, CvType.CV_64F)

            val stdDev = MatOfDouble()
            Core.meanStdDev(laplacianMat, MatOfDouble(), stdDev)
            val variance = stdDev.get(0, 0)[0].pow(2.0)

            return BlurResult(
                isBlurry = variance < threshold,
                blurValue = DecimalFormat("0.00").format(variance).toDouble(),
            )
        } finally {
            sourceMat.release()
            grayMat.release()
            laplacianMat.release()
        }
    }

    data class BlurResult(val isBlurry: Boolean, val blurValue: Double)

    /**
     * Detects glare by counting high-intensity pixels in the image.
     * @param bitmap Input bitmap to check for glare.
     * @param pixelThreshold Grayscale pixel value threshold to consider as glare (default = 240).
     * @param glareRatioThreshold Ratio of bright pixels to total pixels (e.g., 0.05 = 5%).
     */
    fun detectGlare(
        bitmap: Bitmap,
        pixelThreshold: Int = 240,
        glareRatioThreshold: Double = 0.05,
    ): GlareResult {
        val src = Mat()
        val gray = Mat()
        val thresholdMat = Mat()

        try {
            Utils.bitmapToMat(bitmap, src)
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

            Imgproc.threshold(
                gray,
                thresholdMat,
                pixelThreshold.toDouble(),
                255.0,
                Imgproc.THRESH_BINARY,
            )

            val brightPixels = Core.countNonZero(thresholdMat)
            val totalPixels = gray.rows() * gray.cols()
            val ratio = brightPixels.toDouble() / totalPixels.toDouble()

            return GlareResult(
                hasGlare = ratio > glareRatioThreshold,
                glareRatio = DecimalFormat("0.000").format(ratio).toDouble(),
            )
        } finally {
            src.release()
            gray.release()
            thresholdMat.release()
        }
    }

    data class GlareResult(val hasGlare: Boolean, val glareRatio: Double)

    /**
     * Detects tilt angle of the dominant rectangular object (e.g a document).
     *
     * @param bitmap The input image (e.g., document photo).
     * @param angleThreshold Degrees of tilt allowed before it's considered "tilted".
     * @return TiltResult with estimated tilt angle and whether it's acceptable.
     */
    fun detectTilt(bitmap: Bitmap, angleThreshold: Double = 5.0): TiltResult {
        val src = Mat()
        val gray = Mat()
        val edged = Mat()
        val contours = mutableListOf<MatOfPoint>()

        try {
            Utils.bitmapToMat(bitmap, src)
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

            Imgproc.Canny(gray, edged, 50.0, 150.0)

            val contourList = mutableListOf<MatOfPoint>()
            Imgproc.findContours(
                edged,
                contourList,
                Mat(),
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE,
            )

            if (contourList.isEmpty()) {
                return TiltResult(true, 0.0) // No contours = assume upright or undecidable
            }

            val largestContour =
                contourList.maxByOrNull { Imgproc.contourArea(it) } ?: return TiltResult(true, 0.0)

            val contour2f = MatOfPoint2f(*largestContour.toArray())
            val rotatedRect = Imgproc.minAreaRect(contour2f)
            var angle = rotatedRect.angle

            if (angle < -45) {
                angle += 90
            }

            val isUpright = abs(angle) <= angleThreshold

            return TiltResult(!isUpright, DecimalFormat("0.0").format(angle).toDouble())
        } finally {
            src.release()
            gray.release()
            edged.release()
            contours.forEach { it.release() }
        }
    }

    data class TiltResult(val isTilted: Boolean, val tiltAngle: Double)
}
