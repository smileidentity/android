@file:Suppress("unused")

package com.smileidentity.networking.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class SmileIdentityException(val details: Details) : Exception(details.toString()) {

    // This Exception+Details is defined in this way to satisfy Moshi (it doesn't like data classes
    // to have parent classes - i.e. Exception as a parent class)
    @JsonClass(generateAdapter = true)
    data class Details(
        @Json(name = "code") val code: Int,
        @Json(name = "error") val message: String,
    )
}

/**
 * Custom values specific to partners can be placed in [extras]
 */
// The class uses a custom adapter in order to support placing the key-value pairs in [extras] into
// top level fields in the JSON
data class PartnerParams(
    val jobId: String,
    val userId: String,
    val jobType: JobType,
    val extras: Map<String, String> = mapOf(),
)

enum class JobType {
    @Json(name = "4")
    SmartSelfieEnrollment,

    @Json(name = "2")
    SmartSelfieAuthentication,
}
