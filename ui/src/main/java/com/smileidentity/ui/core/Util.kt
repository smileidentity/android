package com.smileidentity.ui.core

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
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.networking.models.SmileIdentityException
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
 * Post-processes the image stored in [file], in-place
 */
internal fun postProcessImageFile(
    file: File,
    saveAsGrayscale: Boolean = false,
    compressionQuality: Int = 100,
    maxOutputSize: Size? = null,
): File {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    return postProcessImageBitmap(
        bitmap,
        file,
        saveAsGrayscale,
        compressionQuality,
        maxOutputSize,
    )
}

/**
 * Save to temporary file, which does not require any storage permissions. It will be saved to the
 * app's cache directory, which is cleared when the app is uninstalled. Images will be saved in the
 * format "si_${imageType}_<random number>.jpg"
 */
internal fun createSmileTempFile(imageType: String): File {
    return File.createTempFile("si_${imageType}_", ".jpg").apply {
        // Deletes file when the *VM* is exited (*not* when the app is closed)
        deleteOnExit()
    }
}

internal fun createLivenessFile() = createSmileTempFile("liveness")
internal fun createSelfieFile() = createSmileTempFile("selfie")

/**
 * Creates a [CoroutineExceptionHandler] that logs the exception, and attempts to convert it to
 * SmileIdentityServerError if it is an [HttpException] (this may not always be possible, i.e. if
 * we get an error during S3 upload, or if we get an unconventional 500 error from the API)
 *
 * @param proxy Callback to be invoked with the exception
 */
internal fun getExceptionHandler(proxy: (Throwable) -> Unit = { }): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Error during coroutine execution")
        val converted = if (throwable is HttpException) {
            try {
                SmileIdentityException(
                    SmileIdentity.retrofit.responseBodyConverter<SmileIdentityException.Details>(
                        SmileIdentityException.Details::class.java,
                        emptyArray(),
                    ).convert(throwable.response()?.errorBody()!!)!!
                )
            } catch (e: Exception) {
                Timber.w(e, "Unable to convert HttpException to SmileIdentityServerError")
                // More informative to pass back the original exception than the conversion error
                throwable
            }
        } else {
            throwable
        }
        proxy(converted)
    }
}
