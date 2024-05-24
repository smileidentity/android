package com.smileidentity.models.v2

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Metadata is a key-value pair that can be used to store additional information about a job
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class Metadata(
    @Json(name = "name") val name: String,
    @Json(name = "value") val value: String,
) : Parcelable
