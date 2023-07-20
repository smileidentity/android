package com.smileidentity.models

import com.smileidentity.SmileID
import com.smileidentity.networking.calculateSignature
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BvnToptRequest(
    @Json(name = "country")
    val country: String,
    @Json(name = "id_number")
    val idNumber: String,
    @Json(name = "id_type")
    val idType: String,
    @Json(name = "partner_id")
    val partnerId: String = SmileID.config.partnerId,
    @Json(name = "timestamp")
    val timestamp: String = System.currentTimeMillis().toString(),
    @Json(name = "signature")
    val signature: String = calculateSignature(timestamp),
)

@JsonClass(generateAdapter = true)
data class BvnToptResponse(
    @Json(name = "message")
    val message: String,
    @Json(name = "modes")
    val modes: List<Mode>,
    @Json(name = "session_id")
    val sessionId: String,
    @Json(name = "signature")
    val signature: String,
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "timestamp")
    val timestamp: String,
)

@JsonClass(generateAdapter = true)
data class Mode(
    @Json(name = "email")
    val email: String,
    @Json(name = "sms")
    val sms: String,
)

@JsonClass(generateAdapter = true)
data class BvnToptModeRequest(
    @Json(name = "country")
    val country: String,
    @Json(name = "id_number")
    val idNumber: String,
    @Json(name = "id_type")
    val idType: String,
    @Json(name = "mode")
    val mode: String,
    @Json(name = "session_id")
    val sessionId: String,
    @Json(name = "partner_id")
    val partnerId: String = SmileID.config.partnerId,
    @Json(name = "timestamp")
    val timestamp: String = System.currentTimeMillis().toString(),
    @Json(name = "signature")
    val signature: String = calculateSignature(timestamp),
)

@JsonClass(generateAdapter = true)
data class BvnToptModeResponse(
    @Json(name = "message")
    val message: String,
    @Json(name = "signature")
    val signature: String,
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "timestamp")
    val timestamp: String,
)

@JsonClass(generateAdapter = true)
data class SubmitBvnToptRequest(
    @Json(name = "country")
    val country: String,
    @Json(name = "id_number")
    val idNumber: String,
    @Json(name = "id_type")
    val idType: String,
    @Json(name = "otp")
    val otp: String,
    @Json(name = "session_id")
    val sessionId: String,
    @Json(name = "partner_id")
    val partnerId: String = SmileID.config.partnerId,
    @Json(name = "timestamp")
    val timestamp: String = System.currentTimeMillis().toString(),
    @Json(name = "signature")
    val signature: String = calculateSignature(timestamp),
)

@JsonClass(generateAdapter = true)
data class SubmitBvnToptResponse(
    @Json(name = "message")
    val message: String,
    @Json(name = "signature")
    val signature: String,
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "timestamp")
    val timestamp: String,
)