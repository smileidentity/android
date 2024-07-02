package com.smileidentity.models.v2

import android.os.Parcelable
import com.smileidentity.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.sealed.annotations.TypeLabel
import kotlinx.parcelize.Parcelize

/**
 * Wrap Metadatum in a list. This allows for easy conversion with Moshi and the format the
 * backend expects
 */
@Parcelize
data class Metadata(val items: List<Metadatum>) : Parcelable {
    companion object {
        fun default(): Metadata = Metadata(listOf(Metadatum.Sdk, Metadatum.SdkVersion))
    }
}

/**
 * key-value pair that can be used to store additional information about a job
 */
@Parcelize
@JsonClass(generateAdapter = true, generator = "sealed:name")
sealed class Metadatum(
    @Json(name = "value") val value: String,
) : Parcelable {
    @Parcelize
    @TypeLabel(label = "sdk")
    data object Sdk : Metadatum("android")

    @Parcelize
    @TypeLabel(label = "sdk_version")
    data object SdkVersion : Metadatum(BuildConfig.VERSION_NAME)

    @Parcelize
    @TypeLabel(label = "document_front_image_origin")
    data class DocumentFrontImageOrigin(val origin: DocumentImageOriginValue) :
        Metadatum(origin.name)

    @Parcelize
    @TypeLabel(label = "document_back_image_origin")
    data class DocumentBackImageOrigin(val origin: DocumentImageOriginValue) :
        Metadatum(origin.name)

    @Parcelize
    @TypeLabel(label = "camera_facing")
    data class CameraFacing(val facing: CameraFacingValue) : Metadatum(facing.name)
}

enum class DocumentImageOriginValue {
    Gallery,
    CameraAutoCapture,
    CameraManualCapture,
}

enum class CameraFacingValue {
    Front,
    Back,
}
