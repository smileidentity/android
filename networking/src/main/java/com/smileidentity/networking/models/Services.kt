package com.smileidentity.networking.models

import com.smileidentity.networking.serializer.RequiredFieldSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServicesResponse(
    @SerialName(value = "bank_codes") val bankCodes: List<BankCode>,
    @SerialName(value = "hosted_web") val hostedWeb: HostedWeb,
)

@Serializable
data class BankCode(
    @SerialName(value = "name") val name: String,
    @SerialName(value = "code") val code: String,
)

@Serializable
data class HostedWeb(
    @SerialName(value = "basic_kyc") internal val _basicKyc: CountryCodeToCountryInfo,
    @SerialName(value = "biometric_kyc") internal val _biometricKyc: CountryCodeToCountryInfo,
    @SerialName(value = "enhanced_kyc") internal val _enhancedKyc: CountryCodeToCountryInfo,
    @SerialName(value = "doc_verification") internal val _docVerification: CountryCodeToCountryInfo,
    @SerialName(value = "ekyc_smartselfie") internal val _enhancedKycSmartSelfie:
    CountryCodeToCountryInfo,
    @SerialName(value = "enhanced_document_verification")
    internal val _enhancedDocumentVerification: CountryCodeToCountryInfo,
) {
    val basicKyc = _basicKyc.toCountryInfo()
    val biometricKyc = _biometricKyc.toCountryInfo()
    val enhancedKyc = _enhancedKyc.toCountryInfo()
    val docVerification = _docVerification.toCountryInfo()
    val enhancedKycSmartSelfie = _enhancedKycSmartSelfie.toCountryInfo()
    val enhancedDocumentVerification = _enhancedDocumentVerification.toCountryInfo()

    /**
     * This is used to convert the [Map] type response to a single object, which makes for a better
     * data model representation
     */
    private fun CountryCodeToCountryInfo.toCountryInfo() = map {
        it.value.copy(
            countryCode = it.key,
        )
    }
}

/**
 * The key is the 2-letter ISO country code for the country. The key should be copied in to
 * [CountryInfo.countryCode]
 */
internal typealias CountryCodeToCountryInfo = Map<String, CountryInfo>

/**
 * The key is the unique identifier for the ID type. The key should be copied in to
 * [AvailableIdType.idTypeKey]
 */
private typealias IdTypeKeyToAvailableIdType = Map<String, AvailableIdType>

/**
 * The [countryCode] field is not populated/returned by the API response, hence it being marked as
 * [Transient]. However, it should be populated before usage of this class.
 */
@Serializable
data class CountryInfo(
    @Transient val countryCode: String = "",
    @SerialName(value = "name") val name: String,
    @SerialName(value = "id_types") internal val _availableIdTypes: IdTypeKeyToAvailableIdType,
) {
    val availableIdTypes = _availableIdTypes.toAvailableIdTypes()

    private fun IdTypeKeyToAvailableIdType.toAvailableIdTypes() = map {
        it.value.copy(idTypeKey = it.key)
    }
}

/**
 * The [idTypeKey] is not populated by the API response, hence being marked as [Transient]. However,
 * it should be populated before usage of this class.
 *
 * [testData], in practice, is only null for BANK_ACCOUNT ID Types
 */
@Serializable
data class AvailableIdType(
    @Transient val idTypeKey: String = "",
    @SerialName(value = "label") val label: String,
    @SerialName(value = "required_fields") val requiredFields: List<RequiredField> = emptyList(),
    @SerialName(value = "test_data") val testData: String?,
    // Don't use a Regex object here directly as that requires us to compile the pattern, which is a
    // heavy operation
    @SerialName(value = "id_number_regex") val idNumberRegex: String?,
)

@Serializable(with = RequiredFieldSerializer::class)
enum class RequiredField {
    @SerialName(value = "id_number")
    IdNumber,

    @SerialName(value = "first_name")
    FirstName,

    @SerialName(value = "last_name")
    LastName,

    @SerialName(value = "dob")
    DateOfBirth,

    @SerialName(value = "day")
    Day,

    @SerialName(value = "month")
    Month,

    @SerialName(value = "year")
    Year,

    @SerialName(value = "bank_code")
    BankCode,

    @SerialName(value = "citizenship")
    Citizenship,

    @SerialName(value = "country")
    Country,

    @SerialName(value = "id_type")
    IdType,

    @SerialName(value = "user_id")
    UserId,

    @SerialName(value = "job_id")
    JobId,

    @SerialName(value = "unknown")
    Unknown,
}
