package com.smileidentity.metadata.models

import android.os.Parcelable
import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.smileidentity.metadata.device.getIPAddress
import com.smileidentity.metadata.device.isProxyDetected
import com.smileidentity.metadata.device.locale
import com.smileidentity.metadata.device.model
import com.smileidentity.metadata.device.os
import com.smileidentity.metadata.device.systemArchitecture
import com.smileidentity.networking.ValueParceler
import com.smileidentity.util.getCurrentIsoTimestamp
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.TimeZone
import kotlin.time.Duration
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.parcelize.TypeParceler

@Parcelize
sealed class Value : Parcelable {
    @Parcelize
    @JsonClass(generateAdapter = true)
    data class StringValue(val value: String) : Value()

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class IntValue(val value: Int) : Value()

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class LongValue(val value: Long) : Value()

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class DoubleValue(val value: Double) : Value()

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class BoolValue(val value: Boolean) : Value()

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class ObjectValue(val map: Map<String, Value>) : Value()

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class ArrayValue(val list: List<Value>) : Value()
}

/**
 * key-value pair that can be used to store additional information about a job
 */
@Parcelize
@JsonClass(generateAdapter = true)
open class Metadatum(
    @Json(name = "name") val name: String,
    @Json(name = "value") val value: @RawValue Value,
    @Json(name = "timestamp") val timestamp: String,
) : Parcelable {
    constructor(
        name: MetadataKey,
        value: String,
        timestamp: String = getCurrentIsoTimestamp(),
    ) : this(name.key, Value.StringValue(value), timestamp)

    constructor(
        name: MetadataKey,
        value: Int,
        timestamp: String = getCurrentIsoTimestamp(),
    ) : this(name.key, Value.IntValue(value), timestamp)

    constructor(
        name: MetadataKey,
        value: Long,
        timestamp: String = getCurrentIsoTimestamp(),
    ) : this(name.key, Value.LongValue(value), timestamp)

    constructor(
        name: MetadataKey,
        value: Double,
        timestamp: String = getCurrentIsoTimestamp(),
    ) : this(name.key, Value.DoubleValue(value), timestamp)

    constructor(
        name: MetadataKey,
        value: Boolean,
        timestamp: String = getCurrentIsoTimestamp(),
    ) : this(name.key, Value.BoolValue(value), timestamp)

    constructor(
        name: MetadataKey,
        value: Map<String, Value>,
        timestamp: String = getCurrentIsoTimestamp(),
    ) : this(name.key, Value.ObjectValue(value), timestamp)

    constructor(
        name: MetadataKey,
        value: List<Value>,
        timestamp: String = getCurrentIsoTimestamp(),
    ) : this(name.key, Value.ArrayValue(value), timestamp)

    @Parcelize
    data class ActiveLivenessType(val type: LivenessType) :
        Metadatum(MetadataKey.ActiveLivenessType, type.value)

    @Parcelize
    data object ActiveLivenessVersion : Metadatum(MetadataKey.ActiveLivenessVersion, "1.0.0")

    @Parcelize
    data class BuildBrand(val buildBrand: String) :
        Metadatum(MetadataKey.BuildBrand, buildBrand)

    @Parcelize
    data class BuildDevice(val buildDevice: String) :
        Metadatum(MetadataKey.BuildDevice, buildDevice)

    @Parcelize
    data class BuildFingerprint(val buildFingerprint: String) :
        Metadatum(MetadataKey.BuildFingerprint, buildFingerprint)

    @Parcelize
    data class BuildHardware(val buildHardware: String) :
        Metadatum(MetadataKey.BuildHardware, buildHardware)

    @Parcelize
    data class BuildProduct(val buildProduct: String) :
        Metadatum(MetadataKey.BuildProduct, buildProduct)

    @Parcelize
    data class BuildSource(val buildSource: String) :
        Metadatum(MetadataKey.BuildSource, buildSource)

    @Parcelize
    data class CameraName(val cameraName: String) :
        Metadatum(MetadataKey.CameraName, cameraName) // todo

    @Parcelize
    data class CarrierInfo(val carrierInfo: String) :
        Metadatum(MetadataKey.CarrierInfo, carrierInfo)

    @TypeParceler<Value, ValueParceler>
    @Parcelize
    data class CertificateDigest(val certificateDigests: List<Value>) :
        Metadatum(MetadataKey.CertificateDigest, certificateDigests)

    @Parcelize
    data object ClientIP : Metadatum(MetadataKey.ClientIP, getIPAddress(useIPv4 = true))

    @Parcelize
    data object DeviceModel : Metadatum(MetadataKey.DeviceModel, model)

    @Parcelize
    data class DeviceMovement(val movementChange: Double) :
        Metadatum(MetadataKey.DeviceMovementDetected, movementChange)

    @Parcelize
    data object DeviceOS : Metadatum(MetadataKey.DeviceOS, os)

    @Parcelize
    data class DeviceOrientation(val orientation: String) :
        Metadatum(MetadataKey.DeviceOrientation, orientation) {

        companion object {
            val PORTRAIT = DeviceOrientation("portrait")
            val LANDSCAPE = DeviceOrientation("landscape")
            val FLAT = DeviceOrientation("flat")
            val UNKNOWN = DeviceOrientation("unknown")
        }
    }

    @Parcelize
    data class DocumentFrontImageOrigin(val origin: DocumentImageOriginValue) :
        Metadatum(MetadataKey.DocumentFrontImageOrigin, origin.value)

    @Parcelize
    data class DocumentBackImageOrigin(val origin: DocumentImageOriginValue) :
        Metadatum(MetadataKey.DocumentBackImageOrigin, origin.value)

    @Parcelize
    data class DocumentFrontCaptureRetries(val retries: Int) :
        Metadatum(MetadataKey.DocumentFrontCaptureRetries, retries)

    @Parcelize
    data class DocumentBackCaptureRetries(val retries: Int) :
        Metadatum(MetadataKey.DocumentBackCaptureRetries, retries)

    /**
     * This represents the time it took for the user to complete *their* portion of the task. It
     * does *NOT* include network time.
     */
    @Parcelize
    data class DocumentFrontCaptureDuration(val duration: Duration) :
        Metadatum(MetadataKey.DocumentFrontCaptureDuration, duration.inWholeMilliseconds)

    /**
     * This represents the time it took for the user to complete *their* portion of the task. It
     * does *NOT* include network time.
     */
    @Parcelize
    data class DocumentBackCaptureDuration(val duration: Duration) :
        Metadatum(MetadataKey.DocumentBackCaptureDuration, duration.inWholeMilliseconds)

    @Parcelize
    data object Fingerprint : Metadatum(MetadataKey.Fingerprint, SmileID.fingerprint)

    @Parcelize
    data class HostApplication(val hostApplication: String) :
        Metadatum(MetadataKey.HostApplication, hostApplication)

    @Parcelize
    data object Locale : Metadatum(MetadataKey.Locale, locale)

    @Parcelize
    data object LocalTimeOfEnrolment :
        Metadatum(
            MetadataKey.LocalTimeOfEnrolment,
            getCurrentIsoTimestamp(TimeZone.getDefault()),
        )

    @TypeParceler<Value, ValueParceler>
    @Parcelize
    data class GeoLocation(val geolocation: Map<String, Value>) :
        Metadatum(MetadataKey.Geolocation, geolocation)

    @Parcelize
    data class MemoryInfo(val memoryInfo: Long) : Metadatum(MetadataKey.MemoryInfo, memoryInfo)

    @Parcelize
    data class NetworkConnection(val networkConnection: String) :
        Metadatum(MetadataKey.NetworkConnection, networkConnection) {

        companion object {
            val WIFI = NetworkConnection("wifi")
            val CELLULAR = NetworkConnection("cellular")
            val OTHER = NetworkConnection("other")
            val UNKNOWN = NetworkConnection("unknown")
        }
    }

    @Parcelize
    data class NetworkRetries(val retries: Int) : Metadatum(MetadataKey.NetworkRetries, retries)

    @Parcelize
    data class NumberOfCameras(val numberOfCameras: Int) :
        Metadatum(MetadataKey.NumberOfCameras, numberOfCameras)

    @Parcelize
    data class PackageName(val packageName: String) :
        Metadatum(MetadataKey.PackageName, packageName)

    @Parcelize
    data class ProximitySensor(val hasProximitySensor: Boolean) :
        Metadatum(MetadataKey.ProximitySensor, hasProximitySensor)

    @Parcelize
    data object Proxy : Metadatum(MetadataKey.ProxyDetected, isProxyDetected())

    @Parcelize
    data class ScreenResolution(val screenResolution: String) :
        Metadatum(MetadataKey.ScreenResolution, screenResolution)

    @Parcelize
    data object SecurityPolicyVersion : Metadatum(MetadataKey.SecurityPolicyVersion, "0.3.0")

    @Parcelize
    data class SelfieImageOrigin(val origin: SelfieImageOriginValue) :
        Metadatum(MetadataKey.SelfieImageOrigin, origin.value)

    /**
     * This represents the time it took for the user to complete *their* portion of the task. It
     * does *NOT* include network time.
     */
    @Parcelize
    data class SelfieCaptureDuration(val duration: Duration) :
        Metadatum(MetadataKey.SelfieCaptureDuration, duration.inWholeMilliseconds)

    @Parcelize
    data class SelfieCaptureRetries(val retries: Int) :
        Metadatum(MetadataKey.SelfieCaptureRetries, retries)

    @Parcelize
    data object Sdk : Metadatum(MetadataKey.Sdk, "android")

    @Parcelize
    data object SdkLaunchCount : Metadatum(MetadataKey.SdkLaunchCount, SmileID.sdkLaunchCount)

    @Parcelize
    data object SdkVersion : Metadatum(MetadataKey.SdkVersion, BuildConfig.VERSION_NAME)

    @Parcelize
    data object SystemArchitecture : Metadatum(MetadataKey.SystemArchitecture, systemArchitecture)

    @Parcelize
    data object Timezone : Metadatum(MetadataKey.Timezone, TimeZone.getDefault().id)

    @Parcelize
    data class Vpn(val vpnDetected: Boolean) : Metadatum(MetadataKey.VPNDetected, vpnDetected)

    @Parcelize
    data object WrapperSdkName : Metadatum(
        MetadataKey.WrapperName,
        SmileID.wrapperSdkName.toString(),
    )

    @Parcelize
    data object WrapperSdkVersion : Metadatum(
        MetadataKey.WrapperVersion,
        SmileID.wrapperSdkVersion.toString(),
    )
}
