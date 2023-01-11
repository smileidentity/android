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
import java.io.File

internal fun Context.toast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

internal val Rect.area get() = height() * width()
internal val InputImage.area get() = height * width

/**
 * Post-processes the image stored in `bitmap` and saves to `file`
 */
internal fun postProcessImageBitmap(
    bitmap: Bitmap,
    file: File,
    saveAsGrayscale: Boolean = false,
    compressionQuality: Int = 100,
    desiredOutputSize: Size? = null,
): File {
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    if (saveAsGrayscale) {
        val canvas = Canvas(mutableBitmap)
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(colorMatrix) }
        canvas.drawBitmap(mutableBitmap, 0f, 0f, paint)
    }
    // if size is the original Bitmap size, then no scaling will be performed by the underlying call
    val size = desiredOutputSize ?: Size(mutableBitmap.width, mutableBitmap.height)
    file.outputStream().use {
        mutableBitmap
            // Filter is set to false for improved performance at the expense of image quality
            .scale(size.width, size.height, filter = false)
            .compress(JPEG, compressionQuality, it)
    }
    return file
}

/**
 * Post-processes the image stored in `file`, in-place
 */
internal fun postProcessImageFile(
    file: File,
    saveAsGrayscale: Boolean = false,
    compressionQuality: Int = 100,
    desiredOutputSize: Size? = null,
): File {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    return postProcessImageBitmap(bitmap,
        file,
        saveAsGrayscale,
        compressionQuality,
        desiredOutputSize)
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
