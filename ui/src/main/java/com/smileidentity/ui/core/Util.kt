package com.smileidentity.ui.core

import android.content.Context
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Size
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.graphics.scale
import java.io.File

internal fun Context.toast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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
    val bitmapOpts = BitmapFactory.Options().apply {
        inMutable = saveAsGrayscale
    }
    val bitmap = BitmapFactory.decodeFile(file.absolutePath, bitmapOpts)
    if (saveAsGrayscale) {
        val canvas = Canvas(bitmap)
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(colorMatrix) }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
    }
    // if size is the original Bitmap size, then no scaling will be performed by the underlying call
    val size = desiredOutputSize ?: Size(bitmap.width, bitmap.height)
    file.outputStream().use {
        bitmap
            // Filter is set to false for improved performance at the expense of image quality
            .scale(size.width, size.height, filter = false)
            .compress(JPEG, compressionQuality, it)
    }
    return file
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
