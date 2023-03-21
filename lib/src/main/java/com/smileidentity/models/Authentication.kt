package com.smileidentity.models

import com.smileidentity.SmileIdentity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthenticationRequest(
    @Json(name = "job_type") val jobType: JobType,
    @Json(name = "enrollment") val enrollment: Boolean,
    @Json(name = "update_enrolled_image") val updateEnrolledImage: Boolean? = null,
    @Json(name = "job_id") val jobId: String? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "signature") val signature: Boolean = true,
    @Json(name = "production") val production: Boolean = !SmileIdentity.useSandbox,
    @Json(name = "partner_id") val partnerId: String = SmileIdentity.config.partnerId,
    @Json(name = "auth_token") val authToken: String = SmileIdentity.config.authToken,
)

@JsonClass(generateAdapter = true)
data class AuthenticationResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "signature") val signature: String,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "partner_params") val partnerParams: PartnerParams,
)
