@file:Suppress("unused")

package com.smileidentity.networking.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PartnerParams(
    @Json(name = "job_type") val jobType: JobType,
    @Json(name = "job_id") val jobId: String,
    @Json(name = "user_id") val userId: String,
)

enum class JobType {
    @Json(name = "4") SmartSelfieEnrollment,
}
