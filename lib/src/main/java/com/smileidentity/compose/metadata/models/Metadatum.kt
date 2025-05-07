package com.smileidentity.compose.metadata.models

import android.os.Parcelable
import com.smileidentity.util.getCurrentIsoTimestamp
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * key-value pair that can be used to store additional information about a job
 */
@Parcelize
@JsonClass(generateAdapter = true)
open class Metadatum(
    @Json(name = "name") val name: String,
    @Json(name = "value") val value: @RawValue Any,
    @Json(name = "timestamp") val timestamp: String,
) : Parcelable {
    constructor(
        name: MetadataKey,
        value: @RawValue Any,
        timestamp: String = getCurrentIsoTimestamp(),
        ) : this(
        name.key, value, timestamp
    )
}

/**
 * A function that can be used to remove an entry in the Metadatum list
 */
fun MutableList<Metadatum>.remove(key: MetadataKey) {
    this.removeAll { it.name == key.key }
}
