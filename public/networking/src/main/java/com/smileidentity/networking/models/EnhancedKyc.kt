package com.smileidentity.networking.models

import com.smileidentity.networking.SmileIdentity
import com.smileidentity.networking.StringifiedBoolean
import com.smileidentity.networking.calculateSignature
import com.squareup.moshi.Json

data class EnhancedKycRequest(
    val country: String, // todo: iso 2 letter code
    val idType: String, // todo: enum
    val idNumber: String,
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val dob: String? = null,
    val phoneNumber: String? = null,
    @Json(name = "partner_params") val partnerParams: PartnerParams,
    @Json(name = "partner_id") val partnerId: String = SmileIdentity.config.partnerId,
    @Json(name = "source_sdk") val sourceSdk: String = "android",
    // TODO: Fetch the version from gradle, once we are set up for distribution
    @Json(name = "source_sdk_version") val sourceSdkVersion: String = "2.0.0",
    @Json(name = "timestamp") val timestamp: String = System.currentTimeMillis().toString(),
    @Json(name = "signature") val signature: String = calculateSignature(timestamp),
)

data class EnhancedKycResponse(
    @Json(name = "SmileJobID") val smileJobId: String,
    @Json(name = "PartnerParams") val partnerParams: PartnerParams,
    @Json(name = "ResultType") val resultType: String, // todo: enum?
    @Json(name = "ResultText") val resultText: String,
    @Json(name = "ResultCode") val resultCode: Int,
    @Json(name = "Actions") val actions: Actions,
    @Json(name = "Country") val country: String, // todo: iso 2 letter code
    @Json(name = "IDType") val idType: String, // todo: enum
    @Json(name = "IDNumber") val idNumber: String,
    @Json(name = "FullName") val fullName: String,
    @Json(name = "ExpirationDate") val expirationDate: String, // todo: date object?
    @Json(name = "DOB") val dob: String, // todo: date object?
    @Json(name = "Photo") val photo: String, // todo: parse base64 string to bytearray?
    @Json(name = "signature") val signature: String,
    @Json(name = "timestamp") val timestamp: String,

    @Json(name = "IsFinalResult") @StringifiedBoolean
    val isFinalResult: Boolean,
)
