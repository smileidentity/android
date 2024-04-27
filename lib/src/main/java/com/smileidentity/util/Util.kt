package com.smileidentity.util

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.os.Bundle
import android.os.Parcelable
import android.util.Size
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.utils.Exif
import com.google.mlkit.vision.common.InputImage
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileID.moshi
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.consent.bvn.BvnOtpVerificationMode
import com.smileidentity.models.BvnVerificationMode
import com.smileidentity.models.SmileIDException
import io.sentry.Breadcrumb
import io.sentry.SentryLevel
import java.io.File
import java.io.Serializable
import kotlin.math.max
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import okio.IOException
import retrofit2.HttpException
import timber.log.Timber

internal fun createBvnOtpVerificationModes(maps: List<BvnVerificationMode>) = maps.flatMap {
    it.entries.map { (mode, value) ->
        BvnOtpVerificationMode(
            mode = value,
            otpSentBy = when (mode) {
                "sms" -> "sms"
                "email" -> "email"
                else -> ""
            },
            description = when (mode) {
                "sms" -> R.string.si_bvn_sms_verification
                "email" -> R.string.si_bvn_email_verification
                else -> R.string.si_bvn_sms_verification
            },
            icon = when (mode) {
                "sms" -> R.drawable.si_bvn_mode_sms
                "email" -> R.drawable.si_bvn_mode_email
                else -> R.drawable.si_bvn_mode_sms
            },
        )
    }
}.toImmutableList()

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
internal fun isImageAtLeast(
    context: Context,
    uri: Uri?,
    width: Int? = 1920,
    height: Int? = 1080,
): Boolean {
    if (uri == null) return false
    val options = Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri).use {
        BitmapFactory.decodeStream(it, null, options)
    }
    val imageHeight = options.outHeight
    val imageWidth = options.outWidth
    return (imageHeight >= (height ?: 0)) && (imageWidth >= (width ?: 0))
}

fun calculateLuminance(imageProxy: ImageProxy): Double {
    // planes[0] is the Y plane aka "luma"
    val data = imageProxy.planes[0].buffer.apply { rewind() }
    var sum = 0.0
    while (data.hasRemaining()) {
        sum += data.get().toInt() and 0xFF
    }
    return sum / data.limit()
}

internal fun isValidDocumentImage(context: Context, uri: Uri?) =
    isImageAtLeast(context, uri, width = 1920, height = 1080)

fun Bitmap.rotated(rotationDegrees: Int, flipX: Boolean = false, flipY: Boolean = false): Bitmap {
    val matrix = Matrix().apply {
        // Rotate the image back to straight.
        postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
    }

    val rotatedBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)

    // Recycle the old bitmap if it has changed.
    if (rotatedBitmap !== this) {
        recycle()
    }
    return rotatedBitmap
}

/**
 * Post-processes the image stored in [bitmap] and saves to [file]. The image is scaled such that
 * the longer dimension equals [resizeLongerDimensionTo] while maintaining the aspect ratio.
 *
 * Only one of [resizeLongerDimensionTo] or [desiredAspectRatio] can be set. Setting both is not
 * supported, and will throw an [IllegalArgumentException].
 */
@SuppressLint("RestrictedApi")
internal fun postProcessImageBitmap(
    bitmap: Bitmap,
    file: File,
    processRotation: Boolean = false,
    @IntRange(from = 0, to = 100) compressionQuality: Int = 100,
    resizeLongerDimensionTo: Int? = null,
    desiredAspectRatio: Float? = null,
): File {
    check(compressionQuality in 0..100) { "Compression quality must be between 0 and 100" }
    if (resizeLongerDimensionTo != null && desiredAspectRatio != null) {
        throw IllegalArgumentException("Only one of maxOutputSize or desiredAspectRatio can be set")
    }

    val matrix = Matrix()
    val didSwapDimensions: Boolean
    if (processRotation) {
        val exif = Exif.createFromFile(file)
        val degrees = exif.rotation
        didSwapDimensions = degrees == 90 || degrees == 270
        val scale = if (exif.isFlippedHorizontally) -1F else 1F
        matrix.postScale(scale, 1F)
        matrix.postRotate(degrees.toFloat())
    } else {
        didSwapDimensions = false
    }

    resizeLongerDimensionTo?.let {
        val maxDimensionSize = max(bitmap.width, bitmap.height)
        val scaleFactor = it.toFloat() / maxDimensionSize
        matrix.postScale(scaleFactor, scaleFactor)
    }

    // Crop height to match desired aspect ratio. This specific behavior is because we force
    // portrait mode when doing document captures, so the image should always be taller than it is
    // wide. If the image is wider than it is tall, then we return as-is
    // For reference, the default aspect ratio of an ID card is around ~1.6
    // NB! This assumes that the portrait mode pic will be taller than it is wide
    val (x, y, newSize) = desiredAspectRatio?.let {
        val width = if (didSwapDimensions) bitmap.height else bitmap.width
        val height = if (didSwapDimensions) bitmap.width else bitmap.height
        if (width > height) {
            return@let Triple(0, 0, Size(bitmap.width, bitmap.height))
        }
        val newHeight = (width / it).toInt().coerceIn(0..height)
        val y = (height - newHeight) / 2
        return@let if (didSwapDimensions) {
            Triple(y, 0, Size(newHeight, width))
        } else {
            Triple(0, y, Size(width, newHeight))
        }
    } ?: Triple(0, 0, Size(bitmap.width, bitmap.height))

    // Center crop the bitmap to the specified croppedHeight and apply the matrix
    file.outputStream().use {
        val compressSuccess = Bitmap.createBitmap(
            bitmap,
            x,
            y,
            newSize.width,
            newSize.height,
            matrix,
            true,
        ).compress(JPEG, compressionQuality, it)
        if (!compressSuccess) {
            SmileIDCrashReporting.hub.addBreadcrumb("Failed to compress bitmap")
            throw IOException("Failed to compress bitmap")
        }
    }
    return file
}

/**
 * Post-processes the image stored in [file] in-place
 */
internal fun postProcessImage(
    file: File,
    processRotation: Boolean = true,
    compressionQuality: Int = 100,
    desiredAspectRatio: Float? = null,
): File {
    val options = Options().apply { inMutable = true }
    val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
    return postProcessImageBitmap(
        bitmap = bitmap,
        file = file,
        processRotation = processRotation,
        compressionQuality = compressionQuality,
        desiredAspectRatio = desiredAspectRatio,
    )
}

/**
 * Creates a [CoroutineExceptionHandler] that logs the exception, and attempts to convert it to
 * [SmileIDException] if it is an [HttpException] (this may not always be possible, i.e. if we get
 * an error during S3 upload, or if we get an unconventional 500 error from the API). Otherwise, the
 * exception is reported back as-is and is also reported to Sentry.
 *
 * @param proxy Callback to be invoked with the exception
 */
fun getExceptionHandler(proxy: (Throwable) -> Unit) = CoroutineExceptionHandler { _, throwable ->
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
            throwable.addSuppressed(e)

            // More informative to pass back the original exception than the conversion error
            throwable
        }
    } else {
        // Unexpected error, report to Sentry
        SmileIDCrashReporting.hub.captureException(throwable) {
            it.level = SentryLevel.INFO
            it.addBreadcrumb(Breadcrumb("Smile ID Coroutine Exception Handler"))
        }
        throwable
    }
    proxy(converted)
}

fun handleOfflineJobFailure(
    jobId: String,
    throwable: Throwable,
    exceptionHandler: (
        (Throwable) -> Unit
    )? = null,
) {
    Timber.e(throwable, "Error in submitJob for jobId: $jobId")
    if (!(SmileID.allowOfflineMode && isNetworkFailure(throwable))) {
        val complete = moveJobToSubmitted(jobId)
        if (!complete) {
            Timber.w("Failed to move job $jobId to complete")
            SmileIDCrashReporting.hub.addBreadcrumb(
                Breadcrumb().apply {
                    category = "Offline Mode"
                    message = "Failed to move job $jobId to complete"
                    level = SentryLevel.INFO
                },
            )
        }
    }
    exceptionHandler?.let { it(throwable) }
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
internal fun generateFileFromUri(uri: Uri, context: Context): File? =
    uri.getFilePath(context = context)?.let { File(it) }

/**
 * Get path from a URI
 *
 * @param context Android context
 */
private fun Uri.getFilePath(context: Context): String? = getImagePath(context, this)

/**
 * Borrowed here - https://gist.github.com/MeNiks/947b471b762f3b26178ef165a7f5558a
 */
private fun getImagePath(context: Context, uri: Uri): String? =
    if ("content".equals(uri.scheme!!, ignoreCase = true)) {
        if (isGooglePhotosUri(uri)) {
            uri.lastPathSegment
        } else {
            getDataColumn(context, uri)
        }
    } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
        uri.path
    } else {
        null
    }

/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context       The context.
 * @param uri           The Uri to query.
 * @return The value of the _data column, which is typically a file path.
 */
private fun getDataColumn(context: Context, uri: Uri?): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)

    try {
        cursor = context.contentResolver.query(uri!!, projection, null, null, null)
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
 * Checks if the given exception is a result of a network failure.
 *
 * This method considers an exception to be a network failure if it is either
 * an [IOException] or an [InterruptedException]. [IOException] typically indicates
 * an issue with network connectivity, such as a timeout or unreachable server.
 * [InterruptedException] is included as a network failure condition here because
 * network operations can be interrupted, especially in a multithreaded context or
 * when a coroutine is cancelled.
 *
 * @param e The exception to check for being indicative of a network failure.
 * @return [Boolean] `true` if the exception is related to a network failure,
 *         `false` otherwise.
 */
internal fun isNetworkFailure(e: Throwable): Boolean = e is IOException || e is InterruptedException

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
private fun isGooglePhotosUri(uri: Uri): Boolean =
    "com.google.android.apps.photos.content" == uri.authority

/**
 * The old getParcelable method is deprecated in API 33 -- use the new one if supported, otherwise
 * fall back to the old one.
 *
 * NB! There is a bug in API 33's implementation (sigh), so actually only use the new API on *34*
 * and beyond (see: https://issuetracker.google.com/issues/242048899)
 *
 * TODO: AndroidX support should be coming for this
 *
 * Implementation from https://stackoverflow.com/a/73311814/3831060
 */
@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? = when {
    SDK_INT >= UPSIDE_DOWN_CAKE -> getParcelable(key, T::class.java)
    else -> getParcelable(key) as? T
}

/**
 * The old getSerializable method is deprecated in API 33 -- use the new one if supported, otherwise
 * fall back to the old one.
 *
 * NB! There is a bug in API 33's implementation (sigh), so actually only use the new API on *34*
 * and beyond (see: https://issuetracker.google.com/issues/242048899)
 *
 * TODO: AndroidX support should be coming for this
 *
 * Implementation from https://stackoverflow.com/a/73311814/3831060
 */

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.getSerializableCompat(key: String): T? = when {
    SDK_INT >= UPSIDE_DOWN_CAKE -> getSerializable(key, T::class.java)
    else -> getSerializable(key) as? T
}
