package com.smileidentity.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class EnhancedDocumentVerificationResponse(
    @Json(name = "Actions")
    val actions: Actions,
    @Json(name = "Country")
    val country: String,
    @Json(name = "DOB")
    val dob: String,
    @Json(name = "Document")
    val document: String,
    @Json(name = "ExpirationDate")
    val expirationDate: String,
    @Json(name = "FullName")
    val fullName: String,
    @Json(name = "Gender")
    val gender: String,
    @Json(name = "IDNumber")
    val idNumber: String,
    @Json(name = "IDType")
    val idType: String,
    @Json(name = "PartnerParams")
    val partnerParams: PartnerParams,
    @Json(name = "ResultCode")
    val resultCode: String,
    @Json(name = "ResultText")
    val resultText: String,
    @Json(name = "signature")
    val signature: String,
    @Json(name = "SmileJobID")
    val smileJobID: String,
    @Json(name = "timestamp")
    val timestamp: String,
) : Parcelable
