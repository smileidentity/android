package com.smileidentity.metadata.models

import android.os.Parcelable
import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.smileidentity.metadata.device.getIPAddress
import com.smileidentity.metadata.device.locale
import com.smileidentity.metadata.device.model
import com.smileidentity.metadata.device.os
import com.smileidentity.metadata.device.systemArchitecture
import com.smileidentity.util.getCurrentIsoTimestamp
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.TimeZone
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
    data class ActiveLivenessType(val type: LivenessType) :
        Metadatum("active_liveness_type", type.value)

    @Parcelize
    data object ActiveLivenessVersion : Metadatum("active_liveness_version", "1.0.0")

    @Parcelize
    data class CameraName(val cameraName: String) :
        Metadatum("camera_name", cameraName) // todo

    @Parcelize
    data class CarrierInfo(val carrierInfo: String) :
        Metadatum("carrier_info", carrierInfo)

    @Parcelize
    data object ClientIP : Metadatum("client_ip", getIPAddress(useIPv4 = true))

    @Parcelize
    data object DeviceModel : Metadatum("device_model", model)

    @Parcelize
    data object DeviceOS : Metadatum("device_os", os)

    @Parcelize
    data class DeviceOrientation(val orientation: String) :
        Metadatum("device_orientation", orientation) {

        companion object {
            val PORTRAIT = DeviceOrientation("portrait")
            val LANDSCAPE = DeviceOrientation("landscape")
            val UNKNOWN = DeviceOrientation("unknown")
        }
    }

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

    @Parcelize
    data object Fingerprint : Metadatum("fingerprint", SmileID.fingerprint)

    @Parcelize
    data class HostApplication(val hostApplication: String) :
        Metadatum("host_application", hostApplication)

    @Parcelize
    data object Locale : Metadatum("locale", locale)

    @Parcelize
    data object LocalTimeOfEnrolment :
        Metadatum("local_time_of_enrolment", getCurrentIsoTimestamp(TimeZone.getDefault()))

    @Parcelize
    data class MemoryInfo(val memoryInfo: String) : Metadatum("memory_info", memoryInfo)

    @Parcelize
    data class NetworkConnection(val networkConnection: String) :
        Metadatum("network_connection", networkConnection)

    @Parcelize
    data class NumberOfCameras(val numberOfCameras: String) :
        Metadatum("number_of_cameras", numberOfCameras)

    @Parcelize
    data class ProximitySensor(val proximitySensor: String) :
        Metadatum("proximity_sensor", proximitySensor)

    @Parcelize
    data class ScreenResolution(val screenResolution: String) :
        Metadatum("screen_resolution", screenResolution)

    @Parcelize
    data object SecurityPolicyVersion : Metadatum("security_policy_version", "0.3.0")

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
    data object Sdk : Metadatum("sdk", "android")

    @Parcelize
    data object SdkVersion : Metadatum("sdk_version", BuildConfig.VERSION_NAME)

    @Parcelize
    data object SystemArchitecture : Metadatum("system_architecture", systemArchitecture)

    @Parcelize
    data object Timezone : Metadatum("timezone", TimeZone.getDefault().id)
}
