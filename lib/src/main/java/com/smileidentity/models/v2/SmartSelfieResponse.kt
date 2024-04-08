package com.smileidentity.models.v2

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class SmartSelfieResponse(
    @Json(name = "code") val code: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "job_id") val jobId: String,
    @Json(name = "job_type") val jobType: JobType,
    @Json(name = "message") val message: String,
    @Json(name = "partner_id") val partnerId: String,
    @Json(name = "partner_params") val partnerParams: Map<String, String>,
    @Json(name = "status") val status: SmartSelfieStatus,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "user_id") val userId: String,
) : Parcelable
