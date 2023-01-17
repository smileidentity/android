@file:Suppress("unused")

package com.smileidentity.networking.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmileIdentityServerError(
    @Json(name = "code") val code: Int,
    @Json(name = "error") val message: String,
)

@JsonClass(generateAdapter = true)
data class PartnerParams(
    @Json(name = "job_id") val jobId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "job_type") val jobType: JobType,
)

enum class JobType {
    @Json(name = "4") SmartSelfieEnrollment,
}
