package com.smileidentity.models

import com.smileidentity.SmileID
import com.smileidentity.networking.calculateSignature
import com.smileidentity.util.getCurrentIsoTimestamp
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

private const val NIGERIA = "NG"
private const val NIGERIA_BVN = "BVN_MFA"

@JsonClass(generateAdapter = true)
data class BvnTotpRequest(
    @Json(name = "country")
    val country: String = NIGERIA,
    @Json(name = "id_number")
    val idNumber: String,
    @Json(name = "id_type")
    val idType: String = NIGERIA_BVN,
    @Json(name = "partner_id")
    val partnerId: String = SmileID.config.partnerId,
    @Json(name = "timestamp")
    val timestamp: String = getCurrentIsoTimestamp(),
    @Json(name = "signature")
    val signature: String = calculateSignature(timestamp),
)

/**
 * BVN Verification Modes (e.g. {"sms": "0800*******67"}
 */
typealias BvnVerificationMode = Map<String, String>

@JsonClass(generateAdapter = true)
data class BvnTotpResponse(
    @Json(name = "message")
    val message: String,
    @Json(name = "modes")
    val modes: List<BvnVerificationMode>,
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
data class BvnTotpModeRequest(
    @Json(name = "country")
    val country: String = NIGERIA,
    @Json(name = "id_number")
    val idNumber: String,
    @Json(name = "id_type")
    val idType: String = NIGERIA_BVN,
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
data class BvnTotpModeResponse(
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
data class SubmitBvnTotpRequest(
    @Json(name = "country")
    val country: String = NIGERIA,
    @Json(name = "id_number")
    val idNumber: String,
    @Json(name = "id_type")
    val idType: String = NIGERIA_BVN,
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
data class SubmitBvnTotpResponse(
    @Json(name = "message")
    val message: String,
    @Json(name = "signature")
    val signature: String,
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "timestamp")
    val timestamp: String,
)
