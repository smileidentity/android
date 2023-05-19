package com.smileidentity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.util.Size
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.graphics.scale
import com.google.mlkit.vision.common.InputImage
import com.smileidentity.SmileID.moshi
import com.smileidentity.models.SmileIDException
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.HttpException
import timber.log.Timber
import java.io.File

internal fun Context.toast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

internal val Rect.area get() = height() * width()
internal val InputImage.area get() = height * width

/**
 * Post-processes the image stored in [bitmap] and saves to [file]. The image is scaled to
 * [maxOutputSize], but maintains the aspect ratio. The image can also converted to grayscale.
 */
internal fun postProcessImageBitmap(
    bitmap: Bitmap,
    file: File,
    saveAsGrayscale: Boolean = false,
    compressionQuality: Int = 100,
    maxOutputSize: Size? = null,
): File {
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    if (saveAsGrayscale) {
        val canvas = Canvas(mutableBitmap)
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(colorMatrix) }
        canvas.drawBitmap(mutableBitmap, 0f, 0f, paint)
    }

    // If size is the original Bitmap size, then no scaling will be performed by the underlying call
    // Aspect ratio will be maintained by retaining the larger dimension
    val size = maxOutputSize ?: Size(mutableBitmap.width, mutableBitmap.height)
    val aspectRatioInput = mutableBitmap.width.toFloat() / mutableBitmap.height
    val aspectRatioMax = size.width.toFloat() / size.height
    var outputWidth = size.width
    var outputHeight = size.height
    if (aspectRatioInput > aspectRatioMax) {
        outputHeight = (outputWidth / aspectRatioInput).toInt()
    } else {
        outputWidth = (outputHeight * aspectRatioInput).toInt()
    }

    file.outputStream().use {
        mutableBitmap
            // Filter is set to false for improved performance at the expense of image quality
            .scale(outputWidth, outputHeight, filter = false)
            .compress(JPEG, compressionQuality, it)
    }
    return file
}

/**
 * Post-processes the image stored in `file`, in-place
 */
internal fun postProcessImage(
    file: File,
    saveAsGrayscale: Boolean = false,
    compressionQuality: Int = 100,
    desiredOutputSize: Size? = null,
): File {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    return postProcessImageBitmap(
        bitmap,
        file,
        saveAsGrayscale,
        compressionQuality,
        desiredOutputSize,
    )
}

// fun detectRectangleWithoutOpenCV(bitmap: Bitmap): Rect? {
//
//     // Convert the Bitmap to a grayscale image
//     val grayscaleImage = bitmap.copy(Bitmap.Config.ALPHA_8, true)
//
//     // Apply a blur to the grayscale image
//     val blurredImage = Bitmap.createScaledBitmap(grayscaleImage, grayscaleImage.width / 2, grayscaleImage.height / 2, false)
//
//     // Apply an edge detector to the blurred image
//     val edges = blurredImage.toGrayscale().apply { threshold(128) }
//
//     // Find the contours in the edges image
//     val contours = edges.findContours()
//
//     // Find the largest contour
//     val largestContour = contours.maxBy { it.size } ?: return null
//
//     // Calculate the bounding box of the largest contour
//     val boundingBox = largestContour.boundingRect()
//
//     return boundingBox
// }
//
// fun Bitmap.toGrayscale(): Bitmap {
//
//     // Create a new grayscale image with the same dimensions as the original image
//     val grayscaleImage = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
//
//     // Get the pixels from the original image
//     val pixels = IntArray(width * height)
//     getPixels(pixels, 0, width, 0, 0, width, height)
//
//     // Convert each pixel to grayscale
//     for (i in pixels.indices) {
//         val red = pixels[i * 4]
//         val green = pixels[i * 4 + 1]
//         val blue = pixels[i * 4 + 2]
//
//         // Calculate the grayscale value
//         val grayscaleValue = (red + green + blue) / 3
//
//         // Set the grayscale value for the pixel
//         pixels[i * 4] = grayscaleValue
//         pixels[i * 4 + 1] = grayscaleValue
//         pixels[i * 4 + 2] = grayscaleValue
//     }
//
//     // Set the pixels for the grayscale image
//     grayscaleImage.setPixels(pixels, 0, width, 0, 0, width, height)
//
//     return grayscaleImage
// }
//
// fun Bitmap.threshold(threshold: Int): Bitmap {
//
//     // Create a new Bitmap with the same dimensions as the original image
//     val thresholdedImage = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
//
//     // Get the pixels from the original image
//     val pixels = IntArray(width * height)
//     getPixels(pixels, 0, width, 0, 0, width, height)
//
//     // Apply the threshold to each pixel
//     for (i in pixels.indices) {
//         val pixel = pixels[i]
//
//         // If the pixel value is greater than or equal to the threshold, set it to white
//         if (pixel >= threshold) {
//             pixels[i] = 255
//         } else {
//             // Otherwise, set it to black
//             pixels[i] = 0
//         }
//     }
//
//     // Set the pixels for the thresholded image
//     thresholdedImage.setPixels(pixels, 0, width, 0, 0, width, height)
//
//     return thresholdedImage
// }
//
// fun Bitmap.findContours(): List<List<Point>> {
//     // Find the contours in the edges image
//     val contours = mutableListOf<List<Point>>()
//     for (x in 0 until width) {
//         for (y in 0 until height) {
//             val pixel = getPixel(x, y)
//
//             // If the pixel value is greater than or equal to the threshold, add the point to the contour
//             if (pixel >= 128) {
//                 contours.lastOrAdd(mutableListOf()).add(Point(x, y))
//             }
//         }
//     }
//
//     return contours
// }
//
// fun detectEdgesByC(bitmap: Bitmap): RectF? {
//     val width = bitmap.width
//     val height = bitmap.height
//     val pixels = IntArray(width * height)
//     bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
//
//     // Sobel edge detection
//     val sxKernel = floatArrayOf(-1.0f, 0.0f, 1.0f, -2.0f, 0.0f, 2.0f, -1.0f, 0.0f, 1.0f)
//     val syKernel = floatArrayOf(-1.0f, -2.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f)
//
//     val edges = BooleanArray(width * height)
//     for (y in 1 until height - 1) {
//         for (x in 1 until width - 1) {
//             var gx = 0.0f
//             var gy = 0.0f
//             for (kernelY in 0..2) {
//                 for (kernelX in 0..2) {
//                     gx += pixels[y * width + x + kernelX - 1] * sxKernel[kernelY * 3 + kernelX]
//                     gy += pixels[y * width + x + kernelX - 1] * syKernel[kernelY * 3 + kernelX]
//                 }
//             }
//             val magnitude = sqrt((gx * gx + gy * gy).toDouble()).toFloat()
//             if (magnitude > 100) edges[y * width + x] = true
//         }
//     }
//
//     // Find bounding box
//     var left = width
//     var top = height
//     var right = 0
//     var bottom = 0
//     for (y in 0 until height) {
//         for (x in 0 until width) {
//             if (edges[y * width + x]) {
//                 left = min(left, x)
//                 top = min(top, y)
//                 right = max(right, x)
//                 bottom = max(bottom, y)
//             }
//         }
//     }
//
//     return if (left < right && top < bottom) RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat()) else null
// }
//
// fun findDocumentBoundsByB(bitmap: Bitmap): Rect? {
//     // Convert the bitmap to grayscale.
//     val grayscaleBitmap = bitmap.toGrayscale()
//
//     // Create the Sobel edge detection kernels.
//     val xKernel = floatArrayOf(-1f, 0f, 1f, -2f, 0f, 2f, -1f, 0f, 1f)
//     val yKernel = floatArrayOf(-1f, -2f, -1f, 0f, 0f, 0f, 1f, 2f, 1f)
//
//     // Apply the Sobel edge detection kernels to the grayscale bitmap.
//     val xGradient = grayscaleBitmap.convolve(xKernel)
//     val yGradient = grayscaleBitmap.convolve(yKernel)
//
//     // Calculate the magnitude of the gradient at each pixel.
//     val magnitude = xGradient.zip(yGradient) { x, y -> Math.sqrt(x * x + y * y) }
//
//     // Find the maximum magnitude in the image.
//     val maxMagnitude = magnitude.maxOrNull() ?: return null
//
//     // Find the pixels that correspond to the maximum magnitude.
//     val maxMagnitudePixels = magnitude.indexOf(maxMagnitude)
//
//     // Calculate the top-left and bottom-right corners of the bounding box.
//     val topLeft = Point(maxMagnitudePixels % bitmap.width, maxMagnitudePixels / bitmap.width)
//     val bottomRight = Point(topLeft.x + 1, topLeft.y + 1)
//
//     // Return the bounding box.
//     return Rect(topLeft, bottomRight)
// }
//
// fun Bitmap.convolve(kernel: FloatArray): Bitmap {
//     // Create a new bitmap with the same size as the input bitmap.
//     val outputBitmap = Bitmap.createBitmap(width, height, config)
//
//     // Loop over each pixel in the output bitmap.
//     for (x in 0 until width) {
//         for (y in 0 until height) {
//             // Calculate the value of the output pixel.
//             val value = kernel.fold(0f) { acc, pixel ->
//                 acc + pixel * getPixel(x + pixel, y + pixel)
//             }
//
//             // Set the value of the output pixel.
//             outputBitmap.setPixel(x, y, value.toInt())
//         }
//     }
//
//     // Return the output bitmap.
//     return outputBitmap
// }

/**
 * Save to temporary file, which does not require any storage permissions. It will be saved to the
 * app's cache directory, which is cleared when the app is uninstalled. Images will be saved in the
 * format `si_${imageType}_<timestamp>.jpg`
 */
internal fun createSmileTempFile(imageType: String, savePath: String = SmileID.fileSavePath): File {
    return File(savePath, "si_${imageType}_${System.currentTimeMillis()}.jpg")
}

internal fun createLivenessFile() = createSmileTempFile("liveness")
internal fun createSelfieFile() = createSmileTempFile("selfie")
internal fun createDocumentFile() = createSmileTempFile("document")

/**
 * Creates a [CoroutineExceptionHandler] that logs the exception, and attempts to convert it to
 * [SmileIDException] if it is an [HttpException] (this may not always be possible, i.e. if we get
 * an error during S3 upload, or if we get an unconventional 500 error from the API)
 *
 * @param proxy Callback to be invoked with the exception
 */
fun getExceptionHandler(proxy: (Throwable) -> Unit): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Error during coroutine execution")
        val converted = if (throwable is HttpException) {
            val adapter = moshi.adapter(SmileIDException.Details::class.java)
            try {
                val details = adapter.fromJson(throwable.response()?.errorBody()?.source()!!)!!
                SmileIDException(details)
            } catch (e: Exception) {
                Timber.w(e, "Unable to convert HttpException to SmileIDException")
                // More informative to pass back the original exception than the conversion error
                throwable
            }
        } else {
            throwable
        }
        proxy(converted)
    }
}

fun randomId(prefix: String) = prefix + "-" + java.util.UUID.randomUUID().toString()
fun randomUserId() = randomId("user")
fun randomJobId() = randomId("job")
