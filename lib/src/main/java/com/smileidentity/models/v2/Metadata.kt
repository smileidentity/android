package com.smileidentity.models.v2

import android.os.Parcelable
import com.smileidentity.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
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
                Metadatum("sdk", "android"),
                Metadatum("sdk_version", BuildConfig.VERSION_NAME),
            ),
        )
    }
}

/**
 * key-value pair that can be used to store additional information about a job
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class Metadatum(
    @Json(name = "name") val name: String,
    @Json(name = "value") val value: String,
) : Parcelable
