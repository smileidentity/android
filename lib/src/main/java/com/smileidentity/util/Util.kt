package com.smileidentity.util

import android.annotation.SuppressLint
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
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.os.Bundle
import android.os.Parcelable
import android.util.Size
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.camera.core.impl.utils.Exif
import androidx.core.graphics.scale
import com.google.mlkit.vision.common.InputImage
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileID.moshi
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.SearchableInputFieldItem
import com.smileidentity.compose.consent.bvn.BvnOtpVerificationMode
import com.smileidentity.models.BvnVerificationMode
import com.smileidentity.models.SmileIDException
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.io.Serializable
import java.nio.ByteBuffer

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

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

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
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri).use {
        BitmapFactory.decodeStream(it, null, options)
    }
    val imageHeight = options.outHeight
    val imageWidth = options.outWidth
    return (imageHeight >= (height ?: 0)) && (imageWidth >= (width ?: 0))
}

internal fun isValidDocumentImage(
    context: Context,
    uri: Uri?,
) = isImageAtLeast(context, uri, width = 1920, height = 1080)

/**
 * Post-processes the image stored in [bitmap] and saves to [file]. The image is scaled to
 * [maxOutputSize], but maintains the aspect ratio. The image can also converted to grayscale.
 *
 * Only one of [maxOutputSize] or [desiredAspectRatio] can be set. Setting both is not supported,
 * and will throw an [IllegalArgumentException].
 */
@SuppressLint("RestrictedApi")
internal fun postProcessImageBitmap(
    bitmap: Bitmap,
    file: File,
    saveAsGrayscale: Boolean = false,
    processRotation: Boolean = false,
    @IntRange(from = 0, to = 100) compressionQuality: Int = 100,
    maxOutputSize: Size? = null,
    desiredAspectRatio: Float? = null,
): File {
    check(compressionQuality in 0..100) { "Compression quality must be between 0 and 100" }
    if (maxOutputSize != null && desiredAspectRatio != null) {
        throw IllegalArgumentException("Only one of maxOutputSize or desiredAspectRatio can be set")
    }
    var mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    if (saveAsGrayscale) {
        val canvas = Canvas(mutableBitmap)
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(colorMatrix) }
        canvas.drawBitmap(mutableBitmap, 0f, 0f, paint)
    }

    if (processRotation) {
        val exif = Exif.createFromFile(file)
        val degrees = exif.rotation.toFloat()
        val scale = if (exif.isFlippedHorizontally) -1F else 1F
        val matrix = Matrix().apply {
            postScale(scale, 1F)
            postRotate(degrees)
        }
        mutableBitmap = Bitmap.createBitmap(
            mutableBitmap,
            0,
            0,
            mutableBitmap.width,
            mutableBitmap.height,
            matrix,
            true,
        )
    }

    // If size is the original Bitmap size, then no scaling will be performed by the underlying call
    // Aspect ratio will be maintained by retaining the larger dimension
    val outputSize = maxOutputSize?.let { size ->
        val aspectRatioInput = mutableBitmap.width.toFloat() / mutableBitmap.height
        val aspectRatioMax = size.width.toFloat() / size.height
        var outputWidth = size.width
        var outputHeight = size.height
        if (aspectRatioInput > aspectRatioMax) {
            outputHeight = (outputWidth / aspectRatioInput).toInt()
        } else {
            outputWidth = (outputHeight * aspectRatioInput).toInt()
        }
        Size(outputWidth, outputHeight)
    }

    // Crop height to match desired aspect ratio. This specific behavior is because we force
    // portrait mode when doing document captures, so the image should always be taller than it is
    // wide. If the image is wider than it is tall, then we return as-is
    // For reference, the default aspect ratio of an ID card is around ~1.6
    // NB! This assumes that the portrait mode pic will be taller than it is wide
    val croppedHeight = desiredAspectRatio?.let {
        return@let if (mutableBitmap.width > mutableBitmap.height) {
            Timber.w("Image is wider than it is tall, so not cropping the height")
            mutableBitmap.height
        } else {
            (mutableBitmap.width / it).toInt().coerceIn(0..mutableBitmap.height)
        }
    } ?: mutableBitmap.height

    file.outputStream().use {
        outputSize?.let { outputSize ->
            // Filter is set to false for improved performance at the expense of image quality
            mutableBitmap = mutableBitmap.scale(outputSize.width, outputSize.height, filter = false)
        }

        desiredAspectRatio?.let {
            // Center crop the bitmap to the specified croppedHeight
            mutableBitmap = Bitmap.createBitmap(
                mutableBitmap,
                0,
                (mutableBitmap.height - croppedHeight) / 2,
                mutableBitmap.width,
                croppedHeight,
            )
        }

        mutableBitmap.compress(JPEG, compressionQuality, it)
    }
    return file
}

/**
 * Post-processes the image stored in `file`, in-place
 */
internal fun postProcessImage(
    file: File,
    saveAsGrayscale: Boolean = false,
    processRotation: Boolean = true,
    compressionQuality: Int = 100,
    desiredAspectRatio: Float? = null,
): File {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    return postProcessImageBitmap(
        bitmap = bitmap,
        file = file,
        saveAsGrayscale = saveAsGrayscale,
        processRotation = processRotation,
        compressionQuality = compressionQuality,
        desiredAspectRatio = desiredAspectRatio,
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
private fun getDataColumn(
    context: Context,
    uri: Uri?,
): String? {
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

internal fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val data = ByteArray(remaining())
    get(data)
    return data
}

val countryDetails = mapOf(
    "AO" to SearchableInputFieldItem("AO", "Angola", "🇦🇴"),
    "BF" to SearchableInputFieldItem("BF", "Burkina Faso", "🇧🇫"),
    "BI" to SearchableInputFieldItem("BI", "Burundi", "🇧🇮"),
    "BJ" to SearchableInputFieldItem("BJ", "Benin", "🇧🇯"),
    "BW" to SearchableInputFieldItem("BW", "Botswana", "🇧🇼"),
    "CD" to SearchableInputFieldItem("CD", "Congo (DRC)", "🇨🇩"),
    "CF" to SearchableInputFieldItem("CF", "Central African Republic", "🇨🇫"),
    "CG" to SearchableInputFieldItem("CG", "Congo", "🇨🇬"),
    "CI" to SearchableInputFieldItem("CI", "Côte d'Ivoire", "🇨🇮"),
    "CM" to SearchableInputFieldItem("CM", "Cameroon", "🇨🇲"),
    "CV" to SearchableInputFieldItem("CV", "Cabo Verde", "🇨🇻"),
    "DJ" to SearchableInputFieldItem("DJ", "Djibouti", "🇩🇯"),
    "EG" to SearchableInputFieldItem("EG", "Egypt", "🇪🇬"),
    "EH" to SearchableInputFieldItem("EH", "Western Sahara", "🇪🇭"),
    "ER" to SearchableInputFieldItem("ER", "Eritrea", "🇪🇷"),
    "ET" to SearchableInputFieldItem("ET", "Ethiopia", "🇪🇹"),
    "GA" to SearchableInputFieldItem("GA", "Gabon", "🇬🇦"),
    "GH" to SearchableInputFieldItem("GH", "Ghana", "🇬🇭"),
    "GM" to SearchableInputFieldItem("GM", "Gambia", "🇬🇲"),
    "GN" to SearchableInputFieldItem("GN", "Guinea", "🇬🇳"),
    "GQ" to SearchableInputFieldItem("GQ", "Equatorial Guinea", "🇬🇶"),
    "GW" to SearchableInputFieldItem("GW", "Guinea-Bissau", "🇬🇼"),
    "KE" to SearchableInputFieldItem("KE", "Kenya", "🇰🇪"),
    "KM" to SearchableInputFieldItem("KM", "Comoros", "🇰🇲"),
    "LR" to SearchableInputFieldItem("LR", "Liberia", "🇱🇷"),
    "LS" to SearchableInputFieldItem("LS", "Lesotho", "🇱🇸"),
    "LY" to SearchableInputFieldItem("LY", "Libya", "🇱🇾"),
    "MA" to SearchableInputFieldItem("MA", "Morocco", "🇲🇦"),
    "MG" to SearchableInputFieldItem("MG", "Madagascar", "🇲🇬"),
    "ML" to SearchableInputFieldItem("ML", "Mali", "🇲🇱"),
    "MR" to SearchableInputFieldItem("MR", "Mauritania", "🇲🇷"),
    "MU" to SearchableInputFieldItem("MU", "Mauritius", "🇲🇺"),
    "MW" to SearchableInputFieldItem("MW", "Malawi", "🇲🇼"),
    "MZ" to SearchableInputFieldItem("MZ", "Mozambique", "🇲🇿"),
    "NA" to SearchableInputFieldItem("NA", "Namibia", "🇳🇦"),
    "NE" to SearchableInputFieldItem("NE", "Niger", "🇳🇪"),
    "NG" to SearchableInputFieldItem("NG", "Nigeria", "🇳🇬"),
    "RW" to SearchableInputFieldItem("RW", "Rwanda", "🇷🇼"),
    "SC" to SearchableInputFieldItem("SC", "Seychelles", "🇸🇨"),
    "SD" to SearchableInputFieldItem("SD", "Sudan", "🇸🇩"),
    "SL" to SearchableInputFieldItem("SL", "Sierra Leone", "🇸🇱"),
    "SN" to SearchableInputFieldItem("SN", "Senegal", "🇸🇳"),
    "SO" to SearchableInputFieldItem("SO", "Somalia", "🇸🇴"),
    "SS" to SearchableInputFieldItem("SS", "South Sudan", "🇸🇸"),
    "ST" to SearchableInputFieldItem("ST", "São Tomé and Príncipe", "🇸🇹"),
    "SZ" to SearchableInputFieldItem("SZ", "Eswatini", "🇸🇿"),
    "TD" to SearchableInputFieldItem("TD", "Chad", "🇹🇩"),
    "TG" to SearchableInputFieldItem("TG", "Togo", "🇹🇬"),
    "TN" to SearchableInputFieldItem("TN", "Tunisia", "🇹🇳"),
    "TZ" to SearchableInputFieldItem("TZ", "Tanzania", "🇹🇿"),
    "UG" to SearchableInputFieldItem("UG", "Uganda", "🇺🇬"),
    "ZA" to SearchableInputFieldItem("ZA", "South Africa", "🇿🇦"),
    "ZM" to SearchableInputFieldItem("ZM", "Zambia", "🇿🇲"),
    "ZW" to SearchableInputFieldItem("ZW", "Zimbabwe", "🇿🇼"),
)