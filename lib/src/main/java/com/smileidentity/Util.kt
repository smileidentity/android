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
import android.net.Uri
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
 * Check if image is at least width x height. Assumes URI is a content URI, which means it needs to
 * be parsed as an input stream and can't be used directly.
 *
 * @param context Android context
 * @param uri Content Uri of the image
 * @param width Minimum width of the image
 * @param height Minimum height of the image
 */
fun isImageAtLeast(
    context: Context,
    uri: Uri?,
    width: Int? = 1920,
    height: Int? = 1080,
): Boolean {
    if (uri == null) return false
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri).use {
        BitmapFactory.decodeStream(it, null, options)
    }
    val imageHeight = options.outHeight
    val imageWidth = options.outWidth
    return (imageHeight >= (height ?: 0)) && (imageWidth >= (width ?: 0))
}

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
 * Save to temporary file, which does not require any storage permissions. It will be saved to the
 * app's cache directory, which is cleared when the app is uninstalled. Images will be saved in the
 * format `si_${imageType}_<timestamp>.jpg`
 */
internal fun createSmileTempFile(imageType: String, savePath: String = SmileID.fileSavePath): File {
    return File(savePath, "si_${imageType}_${System.currentTimeMillis()}.jpg")
}

internal fun createLivenessFile() = createSmileTempFile("liveness")
internal fun createSelfieFile() = createSmileTempFile("selfie")

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
