package com.smileidentity.models

import android.os.Parcelable
import com.smileidentity.SmileID
import com.smileidentity.networking.calculateSignature
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
data class ProductsConfigRequest(
    @Json(name = "partner_id") val partnerId: String = SmileID.config.partnerId,
    @Json(name = "timestamp") val timestamp: String = System.currentTimeMillis().toString(),
    @Json(name = "signature") val signature: String = calculateSignature(timestamp),
)

/**
 * Country Code to ID Type (e.g. {"ZA": ["NATIONAL_ID_NO_PHOTO"]}
 */
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
    @Json(name = "enhanced_document_verification")
    val enhancedDocumentVerification: IdTypes = emptyMap(),
)

@Parcelize
@JsonClass(generateAdapter = true)
data class ValidDocumentsResponse(
    @Json(name = "valid_documents")
    val validDocuments: List<ValidDocument>,
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ValidDocument(
    @Json(name = "country")
    val country: Country,
    @Json(name = "id_types")
    val idTypes: List<IdType>,
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Country(
    @Json(name = "code")
    val code: String,
    @Json(name = "continent")
    val continent: String,
    @Json(name = "name")
    val name: String,
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class IdType(
    @Json(name = "code")
    val code: String,
    @Json(name = "example")
    val example: List<String>,
    @Json(name = "has_back")
    val hasBack: Boolean,
    @Json(name = "name")
    val name: String,
) : Parcelable
