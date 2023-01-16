@file:Suppress("unused")

package com.smileidentity.networking.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrepUploadRequest(
    @Json(name = "source_sdk") val sourceSdk: String = "android",
    @Json(name = "source_sdk_version") val sourceSdkVersion: String = "2.0.0",
    @Json(name = "file_name") val filename: String = "upload.zip",
    @Json(name = "signature") val signature: String,
    @Json(name = "timestamp") val timestamp: String, // TODO: Date
    @Json(name = "smile_client_id") val partnerId: String,
    @Json(name = "partner_params") val partnerParams: PartnerParams,
    @Json(name = "model_parameters") val modelParameters: Map<String, Any> = mapOf(),
    // Callback URL *must* be defined either within your Partner Portal or here
    @Json(name = "callback_url") val callbackUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class PrepUploadResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "ref_id") val refId: String,
    @Json(name = "upload_url") val uploadUrl: String,
    @Json(name = "smile_job_id") val smileJobId: String,
    @Json(name = "camera_config") val cameraConfig: String?,
)
