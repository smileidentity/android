package com.smileidentity.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BvnLookupResponse(
    @Json(name = "entity") val entity: BvnLookupEntity,
)

@JsonClass(generateAdapter = true)
data class BvnLookupEntity(
    @Json(name = "data") val bvn_data: BvnData,
    @Json(name = "status") val status: Int,
)

@JsonClass(generateAdapter = true)
data class BvnData(
    @Json(name = "email") val email: String,
    @Json(name = "message") val message: String,
    @Json(name = "session_id") val session_id: String,
    @Json(name = "sms") val sms: String,
)

@JsonClass(generateAdapter = true)
data class RequestBvnOtpRequest(
    @Json(name = "mode") val mode: String,
    @Json(name = "session_id") val session_id: String,
)

@JsonClass(generateAdapter = true)
data class RequestBvnOtpResponse(
    @Json(name = "entity") val entity: RequestBvnOtpEntity,
)

@JsonClass(generateAdapter = true)
data class RequestBvnOtpEntity(
    @Json(name = "data") val message: String,
    @Json(name = "status") val status: Int,
)

@JsonClass(generateAdapter = true)
data class PostBvnOtpRequest(
    @Json(name = "code") val code: String,
    @Json(name = "session_id") val session_id: String,
)

@JsonClass(generateAdapter = true)
data class PostBvnOtpResponse(
    @Json(name = "entity")
    val entity: PostBvnOtpEntity
)

@JsonClass(generateAdapter = true)
data class PostBvnOtpEntity(
    @Json(name = "data") val message: String,
    @Json(name = "status") val status: Int,
)
