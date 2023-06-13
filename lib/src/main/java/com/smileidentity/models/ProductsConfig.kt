package com.smileidentity.models

import com.smileidentity.SmileID
import com.smileidentity.networking.calculateSignature
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductsConfigRequest(
    @Json(name = "partner_id") val partnerId: String = SmileID.config.partnerId,
    @Json(name = "timestamp") val timestamp: String = System.currentTimeMillis().toString(),
    @Json(name = "signature") val signature: String = calculateSignature(timestamp),
)

// Country Code to ID Type (e.g. {"ZA": ["NATIONAL_ID_NO_PHOTO"]}
typealias IdTypes = Map<String, List<String>>

@JsonClass(generateAdapter = true)
data class ProductsConfigResponse(
    @Json(name = "consentRequired") val consentRequired: IdTypes = emptyMap(),
    @Json(name = "idSelection") val idSelection: IdSelection = IdSelection(),
)

@JsonClass(generateAdapter = true)
data class IdSelection(
    @Json(name = "basic_kyc") val basicKyc: IdTypes = emptyMap(),
    @Json(name = "biometric_kyc") val biometricKyc: IdTypes = emptyMap(),
    @Json(name = "enhanced_kyc") val enhancedKyc: IdTypes = emptyMap(),
    @Json(name = "doc_verification") val documentVerification: IdTypes = emptyMap(),
)
