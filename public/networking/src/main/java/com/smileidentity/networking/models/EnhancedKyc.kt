package com.smileidentity.networking.models

import com.smileidentity.networking.SmileIdentity
import com.smileidentity.networking.StringifiedBoolean
import com.smileidentity.networking.calculateSignature
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EnhancedKycRequest(
    @Json(name = "country") val country: String,
    @Json(name = "id_type") val idType: String,
    @Json(name = "id_number") val idNumber: String,
    @Json(name = "first_name") val firstName: String? = null,
    @Json(name = "middle_name") val middleName: String? = null,
    @Json(name = "last_name") val lastName: String? = null,
    @Json(name = "dob") val dob: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    @Json(name = "bank_code") val bankCode: String? = null,
    @Json(name = "partner_params") val partnerParams: PartnerParams = PartnerParams(),
    @Json(name = "partner_id") val partnerId: String = SmileIdentity.config.partnerId,
    @Json(name = "source_sdk") val sourceSdk: String = "android",
    // TODO: Fetch the version from gradle, once we are set up for distribution
    @Json(name = "source_sdk_version") val sourceSdkVersion: String = "2.0.0",
    @Json(name = "timestamp") val timestamp: String = System.currentTimeMillis().toString(),
    @Json(name = "signature") val signature: String = calculateSignature(timestamp),
)

@JsonClass(generateAdapter = true)
data class EnhancedKycResponse(
    @Json(name = "SmileJobID") val smileJobId: String,
    @Json(name = "PartnerParams") val partnerParams: PartnerParams,
    @Json(name = "ResultType") val resultType: String,
    @Json(name = "ResultText") val resultText: String,
    @Json(name = "ResultCode") val resultCode: Int,
    @Json(name = "Actions") val actions: Actions,
    @Json(name = "Country") val country: String,
    @Json(name = "IDType") val idType: String,
    @Json(name = "IDNumber") val idNumber: String,
    @Json(name = "FullName") val fullName: String?,
    @Json(name = "ExpirationDate") val expirationDate: String?,
    @Json(name = "DOB") val dob: String?,
    @Json(name = "Photo") val base64Photo: String?,

    @Json(name = "IsFinalResult") @StringifiedBoolean
    val isFinalResult: Boolean,
)

enum class IdType(
    val countryCode: String,
    val idType: String,
    val idNumberRegex: String,
    val requiredFields: List<InputField> = listOf(InputField.IdNumber),
    val supportsBasicKyc: Boolean = true,
    val supportsEnhancedKyc: Boolean = true,
    val supportsBiometricKyc: Boolean = true,
) {
    GhanaDriversLicense("GH", "DRIVERS_LICENSE", "(?i)^[a-zA-Z0-9!-]{6,18}$"),
    GhanaPassport("GH", "PASSPORT", "(?i)^[A-Z][0-9]{7,9}$"),
    GhanaSSNIT("GH", "SSNIT", "(?i)^[a-zA-Z]{1}[a-zA-Z0-9]{12,14}$"),
    GhanaVoterId("GH", "VOTER_ID", "(?i)^[0-9]{10,12}$"),
    GhanaNewVoterId("GH", "NEW_VOTER_ID", "^[0-9]{10,12}$"),

    KenyaAlienCard("KE", "ALIEN_CARD", "^[0-9]{6,9}$"),
    KenyaNationalId("KE", "NATIONAL_ID", "^[0-9]{1,9}$"),
    KenyaNationalIdNoPhoto(
        "KE",
        "NATIONAL_ID_NO_PHOTO",
        "^[0-9]{1,9}$",
        supportsBiometricKyc = false,
    ),
    KenyaPassport("KE", "PASSPORT", "^[A-Z0-9]{7,9}$"),

    NigeriaBankAccount(
        "NG",
        "BANK_ACCOUNT",
        "^[0-9]{10}$",
        requiredFields = listOf(InputField.IdNumber, InputField.BankCode),
        supportsBiometricKyc = false,
    ),
    NigeriaBVN("NG", "BVN", "/^[0-9]{11}$/"),
    NigeriaDriversLicense(
        "NG",
        "DRIVERS_LICENSE",
        "(?i)^[a-zA-Z]{3}([ -]{1})?[A-Z0-9]{6,12}$",
        requiredFields = listOf(
            InputField.IdNumber,
            InputField.FirstName,
            InputField.LastName,
            InputField.Dob,
        ),
    ),
    NigeriaNINV2("NG", "NIN_V2", "^[0-9]{11}$"),
    NigeriaNINSlip("NG", "NIN_SLIP", "^[0-9]{11}$"),
    NigeriaVNIN("NG", "V_NIN", "(?i)^[A-Z0-9]{16}$"),
    NigeriaPhoneNumber(
        "NG",
        "PHONE_NUMBER",
        "/^[0-9]{11}$/",
        supportsEnhancedKyc = false,
        supportsBiometricKyc = false,
        // TODO: Check if phone_number field is explicitly required,
        //  or if ID Number *is* the phone number
    ),
    NigeriaVoterId("NG", "VOTER_ID", "(?i)^[A-Z0-9 ]{9,20}$"),

    SouthAfricaNationalId(
        "ZA",
        "NATIONAL_ID",
        "^[0-9]{13}$",
        supportsBasicKyc = false,
    ),
    SouthAfricaNationalIdNoPhoto(
        "ZA",
        "NATIONAL_ID_NO_PHOTO",
        "^[0-9]{13}$",
        supportsBiometricKyc = false,
    ),

    UgandaNationalIdNoPhoto(
        "UG",
        "NATIONAL_ID_NO_PHOTO",
        "(?i)^[A-Z0-9]{14}$",
    ),
    ;

    enum class InputField {
        IdNumber,
        FirstName,
        LastName,
        Dob,
        BankCode,
    }

    fun isValidIdNumber(idNumber: String) = idNumber matches idNumberRegex.toRegex()
}
