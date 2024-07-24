@file:Suppress("unused")

package com.smileidentity.models

import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.networking.calculateSignature
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrepUploadRequest(
    @Json(name = "partner_params") val partnerParams: PartnerParams,
    // Callback URL *must* be defined either within your Partner Portal or here
    @Json(name = "callback_url") val callbackUrl: String? = SmileID.callbackUrl,
    // TODO - Michael will change this to a boolean
    @Json(name = "allow_new_enroll") val allowNewEnroll: String,
    @Json(name = "smile_client_id") val partnerId: String = SmileID.config.partnerId,
    @Json(name = "metadata") val metadata: List<Metadatum>? = null,
    @Json(name = "retry") val retry: Boolean? = null,
    @Json(name = "source_sdk") val sourceSdk: String = "android",
    @Json(name = "source_sdk_version") val sourceSdkVersion: String = BuildConfig.VERSION_NAME,
    @Json(name = "timestamp") val timestamp: String = System.currentTimeMillis().toString(),
    @Json(name = "signature") val signature: String = calculateSignature(timestamp),
)

@JsonClass(generateAdapter = true)
data class PrepUploadResponse(
    @Json(name = "code") val code: String,
    @Json(name = "ref_id") val refId: String,
    @Json(name = "upload_url") val uploadUrl: String,
    @Json(name = "smile_job_id") val smileJobId: String,
)
