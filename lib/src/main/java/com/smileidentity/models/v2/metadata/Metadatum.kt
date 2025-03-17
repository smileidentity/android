package com.smileidentity.models.v2.metadata

import android.os.Parcelable
import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.time.Duration
import kotlinx.parcelize.Parcelize

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
    data class ActiveLivenessType(val type: LivenessType) :
        Metadatum("active_liveness_type", type.value)

    @Parcelize
    data object ActiveLivenessVersion : Metadatum("active_liveness_version", "1.0.0")

    @Parcelize
    data object ClientIP : Metadatum("client_ip", getIPAddress(useIPv4 = true))

    @Parcelize
    data object DeviceModel : Metadatum("device_model", model)

    @Parcelize
    data object DeviceOS : Metadatum("device_os", os)

    @Parcelize
    data object Fingerprint : Metadatum("fingerprint", SmileID.fingerprint)

    @Parcelize
    data class CameraName(val cameraName: String) :
        Metadatum("camera_name", cameraName)

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

enum class LivenessType(val value: String) {
    HeadPose("head_pose"),

    Smile("smile"),
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
