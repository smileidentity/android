package com.smileidentity.networking.models

import com.smileidentity.networking.util.calculateSignature
import com.smileidentity.networking.util.getCurrentIsoTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductsConfigRequest(
    @SerialName(value = "partner_id") val partnerId: String,
    @SerialName(value = "timestamp") val timestamp: String = getCurrentIsoTimestamp(),
    @SerialName(value = "signature") val signature: String = calculateSignature(timestamp),
)

/**
 * Country Code to ID Type (e.g. {"ZA": ["NATIONAL_ID_NO_PHOTO"]}
 */
typealias IdTypes = Map<String, List<String>>

@Serializable
data class ProductsConfigResponse(
    @SerialName(value = "consentRequired") val consentRequired: IdTypes = emptyMap(),
    @SerialName(value = "idSelection") val idSelection: IdSelection = IdSelection(),
)

@Serializable
data class IdSelection(
    @SerialName(value = "basic_kyc") val basicKyc: IdTypes = emptyMap(),
    @SerialName(value = "biometric_kyc") val biometricKyc: IdTypes = emptyMap(),
    @SerialName(value = "enhanced_kyc") val enhancedKyc: IdTypes = emptyMap(),
    @SerialName(value = "doc_verification") val documentVerification: IdTypes = emptyMap(),
)

@Serializable
data class ValidDocumentsResponse(
    @SerialName(value = "valid_documents") val validDocuments: List<ValidDocument>,
)

@Serializable
data class ValidDocument(
    @SerialName(value = "country") val country: Country,
    @SerialName(value = "id_types") val idTypes: List<IdType>,
)

@Serializable
data class Country(
    @SerialName(value = "code") val code: String,
    @SerialName(value = "continent") val continent: String,
    @SerialName(value = "name") val name: String,
)

@Serializable
data class IdType(
    @SerialName(value = "code") val code: String,
    @SerialName(value = "example") val example: List<String>,
    @SerialName(value = "has_back") val hasBack: Boolean,
    @SerialName(value = "name") val name: String,
)
