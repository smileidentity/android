package com.smileidentity

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Size
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.camera.core.impl.utils.Exif
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
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
@SuppressLint("RestrictedApi")
internal fun postProcessImageBitmap(
    bitmap: Bitmap,
    file: File,
    saveAsGrayscale: Boolean = false,
    processRotation: Boolean = false,
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

    if (processRotation) {
        val exif = Exif.createFromFile(file)
        val degrees = when (exif.rotation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90F
            ExifInterface.ORIENTATION_ROTATE_180 -> 180F
            ExifInterface.ORIENTATION_ROTATE_270 -> 270F
            else -> 90F
        }

        val matrix = Matrix().apply { postRotate(degrees) }
        val bitmap = Bitmap.createBitmap(
            mutableBitmap,
            0,
            0,
            mutableBitmap.width,
            mutableBitmap.height,
            matrix,
            true,
        )
        file.outputStream().use {
            bitmap
                .compress(JPEG, compressionQuality, it)
        }
        return file
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
    isDocumentVerification: Boolean = true,
    compressionQuality: Int = 100,
    desiredOutputSize: Size? = null,
): File {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    return postProcessImageBitmap(
        bitmap = bitmap,
        file = file,
        saveAsGrayscale = saveAsGrayscale,
        processRotation = isDocumentVerification,
        compressionQuality = compressionQuality,
        maxOutputSize = desiredOutputSize,
    )
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
internal fun createDocumentFile() = createSmileTempFile("document")

/**
 * Creates a [CoroutineExceptionHandler] that logs the exception, and attempts to convert it to
 * [SmileIDException] if it is an [HttpException] (this may not always be possible, i.e. if we get
 * an error during S3 upload, or if we get an unconventional 500 error from the API). Otherwise, the
 * exception is reported back as-is and is also reported to Sentry.
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

                // Report the *conversion* error to Sentry, rather than the original error
                SmileIDCrashReporting.hub.captureException(e)

                // More informative to pass back the original exception than the conversion error
                throwable
            }
        } else {
            // Unexpected error, report to Sentry
            SmileIDCrashReporting.hub.captureException(throwable)
            throwable
        }
        proxy(converted)
    }
}

fun randomId(prefix: String) = prefix + "-" + java.util.UUID.randomUUID().toString()
fun randomUserId() = randomId("user")
fun randomJobId() = randomId("job")

/**
 * This code gets the real path/ sd card path from intent data, and handles every possible scenario
 * and edge cases, on multiple devices.
 *
 * This replaces uri.toFile() in normal scenarios
 *
 * Gist - https://gist.github.com/MeNiks/947b471b762f3b26178ef165a7f5558a
 *
 *  @param uri a URI
 *  @param context Android Context
 */
internal fun generateFileFromUri(
    uri: Uri,
    context: Context,
): File? = uri.getFilePath(context = context)?.let { File(it) }

/**
 * Get path from a URI
 *
 * @param context Android context
 */
private fun Uri.getFilePath(context: Context): String? =
    getImagePath(context, this)

/**
 * Borrowed here - https://gist.github.com/MeNiks/947b471b762f3b26178ef165a7f5558a
 */
private fun getImagePath(context: Context, uri: Uri): String? {
    if (DocumentsContract.isDocumentUri(context, uri)) {
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
        } else if (isDownloadsDocument(uri)) {
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(
                    uri,
                    arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                    null,
                    null,
                    null,
                )
                cursor!!.moveToNext()
                val fileName = cursor.getString(0)
                val path =
                    Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName
                if (!TextUtils.isEmpty(path)) {
                    return path
                }
            } finally {
                cursor?.close()
            }
            val id = DocumentsContract.getDocumentId(uri)
            if (id.startsWith("raw:")) {
                return id.replaceFirst("raw:".toRegex(), "")
            }
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads"),
                java.lang.Long.valueOf(id),
            )

            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            var contentUri: Uri? = null
            when (type) {
                "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])

            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
        return if (isGooglePhotosUri(uri)) {
            uri.lastPathSegment
        } else {
            getDataColumn(
                context,
                uri,
                null,
                null,
            )
        }
    } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
        return uri.path
    }

    return null
}

/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context       The context.
 * @param uri           The Uri to query.
 * @param selection     (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 */
private fun getDataColumn(
    context: Context,
    uri: Uri?,
    selection: String?,
    selectionArgs: Array<String>?,
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)

    try {
        cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
private fun isExternalStorageDocument(uri: Uri): Boolean =
    "com.android.externalstorage.documents" == uri.authority

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
private fun isDownloadsDocument(uri: Uri): Boolean =
    "com.android.providers.downloads.documents" == uri.authority

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
private fun isMediaDocument(uri: Uri): Boolean =
    "com.android.providers.media.documents" == uri.authority

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
private fun isGooglePhotosUri(uri: Uri): Boolean =
    "com.google.android.apps.photos.content" == uri.authority
