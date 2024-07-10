package com.smileidentity.models.v2

import android.os.Build
import android.os.Parcelable
import com.serjltt.moshi.adapters.FallbackEnum
import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.time.Duration
import kotlinx.parcelize.Parcelize

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
                Metadatum.Fingerprint,
                Metadatum.DeviceModel,
                Metadatum.DeviceOS,
            ),
        )
    }
}

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
    data object DeviceModel : Metadatum("device_model", model)

    @Parcelize
    data object DeviceOS : Metadatum("device_os", os)

    @Parcelize
    data object Fingerprint : Metadatum("fingerprint", SmileID.fingerprint)

    @Parcelize
    data class CameraFacing(val facing: CameraFacingValue) : Metadatum("camera_facing", facing.name)

    /**
     * This represents the time it took for the user to complete *their* portion of the task. It
     * does *NOT* include network time.
     */
    @Parcelize
    data class SelfieCaptureDuration(val duration: Duration) :
        Metadatum("selfie_capture_duration", duration.toIsoString())

    @Parcelize
    data class DocumentFrontImageOrigin(val origin: DocumentImageOriginValue) :
        Metadatum("document_front_image_origin", origin.name)

    @Parcelize
    data class DocumentBackImageOrigin(val origin: DocumentImageOriginValue) :
        Metadatum("document_back_image_origin", origin.name)

    @Parcelize
    data class FrontDocumentCaptureRetries(val retries: Int) :
        Metadatum("front_document_capture_retries", retries.toString())

    @Parcelize
    data class BackDocumentCaptureRetries(val retries: Int) :
        Metadatum("back_document_capture_retries", retries.toString())

    /**
     * This represents the time it took for the user to complete *their* portion of the task. It
     * does *NOT* include network time.
     */
    @Parcelize
    data class FrontDocumentCaptureDuration(val duration: Duration) :
        Metadatum("front_document_capture_duration", duration.toIsoString())

    /**
     * This represents the time it took for the user to complete *their* portion of the task. It
     * does *NOT* include network time.
     */
    @Parcelize
    data class BackDocumentCaptureDuration(val duration: Duration) :
        Metadatum("back_document_capture_duration", duration.toIsoString())
}

@FallbackEnum(name = "Gallery")
enum class DocumentImageOriginValue {
    @Json(name = "gallery")
    Gallery,

    @Json(name = "camera_auto_capture")
    CameraAutoCapture,

    @Json(name = "camera_manual_capture")
    CameraManualCapture,
}

@FallbackEnum(name = "Front")
enum class CameraFacingValue {
    @Json(name = "front")
    Front,

    @Json(name = "back")
    Back,
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
