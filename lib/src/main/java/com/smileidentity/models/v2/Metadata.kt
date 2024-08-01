package com.smileidentity.models.v2

import android.os.Build
import android.os.Parcelable
import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale
import kotlin.time.Duration
import kotlinx.parcelize.Parcelize
import timber.log.Timber

/**
 * Wrap Metadatum in a list. This allows for easy conversion with Moshi and the format the
 * backend expects
 */
@Parcelize
data class Metadata(val items: List<Metadatum>) : Parcelable {
    companion object {
        fun default(): Metadata = Metadata(
            listOf(
                Metadatum.Sdk,
                Metadatum.SdkVersion,
                Metadatum.ClientIP,
                Metadatum.Fingerprint,
                Metadatum.DeviceModel,
                Metadatum.DeviceOS,
            ),
        )
    }
}

fun List<Metadatum>.asNetworkRequest(): Metadata = Metadata(this)

/**
 * key-value pair that can be used to store additional information about a job
 */
@Parcelize
@JsonClass(generateAdapter = true)
open class Metadatum(
    @Json(name = "name") val name: String,
    @Json(name = "value") val value: String,
) : Parcelable {
    @Parcelize
    data object Sdk : Metadatum("sdk", "android")

    @Parcelize
    data object SdkVersion : Metadatum("sdk_version", BuildConfig.VERSION_NAME)

    @Parcelize
    data object ClientIP : Metadatum("client_ip", getIPAddress(useIPv4 = true))

    @Parcelize
    data object DeviceModel : Metadatum("device_model", model)

    @Parcelize
    data object DeviceOS : Metadatum("device_os", os)

    @Parcelize
    data object Fingerprint : Metadatum("fingerprint", SmileID.fingerprint)

    @Parcelize
    data class SelfieImageOrigin(val origin: SelfieImageOriginValue) :
        Metadatum("selfie_image_origin", origin.value)

    /**
     * This represents the time it took for the user to complete *their* portion of the task. It
     * does *NOT* include network time.
     */
    @Parcelize
    data class SelfieCaptureDuration(val duration: Duration) :
        Metadatum("selfie_capture_duration_ms", duration.inWholeMilliseconds.toString())

    @Parcelize
    data class DocumentFrontImageOrigin(val origin: DocumentImageOriginValue) :
        Metadatum("document_front_image_origin", origin.value)

    @Parcelize
    data class DocumentBackImageOrigin(val origin: DocumentImageOriginValue) :
        Metadatum("document_back_image_origin", origin.value)

    @Parcelize
    data class DocumentFrontCaptureRetries(val retries: Int) :
        Metadatum("document_front_capture_retries", retries.toString())

    @Parcelize
    data class DocumentBackCaptureRetries(val retries: Int) :
        Metadatum("document_back_capture_retries", retries.toString())

    /**
     * This represents the time it took for the user to complete *their* portion of the task. It
     * does *NOT* include network time.
     */
    @Parcelize
    data class DocumentFrontCaptureDuration(val duration: Duration) :
        Metadatum("document_front_capture_duration_ms", duration.inWholeMilliseconds.toString())

    /**
     * This represents the time it took for the user to complete *their* portion of the task. It
     * does *NOT* include network time.
     */
    @Parcelize
    data class DocumentBackCaptureDuration(val duration: Duration) :
        Metadatum("document_back_capture_duration_ms", duration.inWholeMilliseconds.toString())
}

enum class DocumentImageOriginValue(val value: String) {
    Gallery("gallery"),

    CameraAutoCapture("camera_auto_capture"),

    CameraManualCapture("camera_manual_capture"),
}

enum class SelfieImageOriginValue(val value: String) {
    FrontCamera("front_camera"),

    BackCamera("back_camera"),
}

private val isEmulator: Boolean
    get() {
        try {
            return (
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                    Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.PRODUCT.contains("sdk_google") ||
                    Build.PRODUCT.contains("google_sdk") ||
                    Build.PRODUCT.contains("sdk") ||
                    Build.PRODUCT.contains("sdk_x86") ||
                    Build.PRODUCT.contains("sdk_gphone64_arm64") ||
                    Build.PRODUCT.contains("vbox86p") ||
                    Build.PRODUCT.contains("emulator") ||
                    Build.PRODUCT.contains("simulator")
                )
        } catch (e: Exception) {
            return false
        }
    }

/**
 * Returns the model of the device. If the device is an emulator, it returns "emulator". Any errors
 * result in "unknown"
 */
private val model: String
    get() {
        try {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (isEmulator) {
                "emulator"
            } else if (model.contains(manufacturer, ignoreCase = true)) {
                model
            } else {
                "$manufacturer $model"
            }
        } catch (e: Exception) {
            Timber.w(e, "Error getting device model")
            SmileIDCrashReporting.hub.addBreadcrumb("Error getting device model: $e")
            return "unknown"
        }
    }

/**
 * On Android, we return the API level, as this provides much more signal than the consumer facing
 * version number
 */
private val os: String
    get() = "Android API ${Build.VERSION.SDK_INT}"

fun getIPAddress(useIPv4: Boolean): String {
    return try {
        val networkInterfaces: List<NetworkInterface> = Collections.list(
            NetworkInterface.getNetworkInterfaces(),
        )
        for (networkInterface in networkInterfaces) {
            val addresses: List<InetAddress> = Collections.list(networkInterface.inetAddresses)
            for (item in addresses) {
                if (!item.isLoopbackAddress) {
                    val address = item.hostAddress
                    val isIPv4 = address.indexOf(':') < 0

                    if (useIPv4) {
                        if (isIPv4) {
                            return address
                        }
                    } else {
                        if (!isIPv4) {
                            val delim = address.indexOf('%') // drop ip6 zone suffix
                            return if (delim < 0) {
                                address.uppercase(Locale.getDefault())
                            } else {
                                address.substring(0, delim).uppercase(Locale.getDefault())
                            }
                        }
                    }
                }
            }
        }
        "" // Return empty string if no IP address is found
    } catch (ex: Exception) {
        "" // Return empty string on exception
    }
}
