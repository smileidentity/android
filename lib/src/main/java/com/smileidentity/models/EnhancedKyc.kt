package com.smileidentity.models

import android.os.Parcelable
import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.smileidentity.networking.calculateSignature
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
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
    @Json(name = "partner_params") val partnerParams: PartnerParams,
    @Json(name = "partner_id") val partnerId: String = SmileID.config.partnerId,
    @Json(name = "source_sdk") val sourceSdk: String = "android",
    @Json(name = "source_sdk_version") val sourceSdkVersion: String = BuildConfig.VERSION_NAME,
    @Json(name = "timestamp") val timestamp: String = System.currentTimeMillis().toString(),
    @Json(name = "signature") val signature: String = calculateSignature(timestamp),
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class EnhancedKycResponse(
    @Json(name = "SmileJobID") val smileJobId: String,
    @Json(name = "PartnerParams") val partnerParams: PartnerParams,
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
) : Parcelable

// TODO: consolidate with Document?
enum class IdType(
    val countryCode: String,
    val idType: String,
    val requiredFields: List<InputField> = listOf(InputField.IdNumber),
    val supportsBasicKyc: Boolean = true,
    val supportsEnhancedKyc: Boolean = true,
    val supportsBiometricKyc: Boolean = true,
) {
    GhanaDriversLicense("GH", "DRIVERS_LICENSE"),
    GhanaPassport("GH", "PASSPORT"),
    GhanaSSNIT("GH", "SSNIT"),
    GhanaVoterId("GH", "VOTER_ID"),
    GhanaNewVoterId("GH", "NEW_VOTER_ID"),

    KenyaAlienCard("KE", "ALIEN_CARD"),
    KenyaNationalId("KE", "NATIONAL_ID"),
    KenyaNationalIdNoPhoto(
        "KE",
        "NATIONAL_ID_NO_PHOTO",
        supportsBiometricKyc = false,
    ),
    KenyaPassport("KE", "PASSPORT"),

    NigeriaBankAccount(
        "NG",
        "BANK_ACCOUNT",
        requiredFields = listOf(InputField.IdNumber, InputField.BankCode),
        supportsBiometricKyc = false,
    ),
    NigeriaBVN("NG", "BVN"),
    NigeriaDriversLicense(
        "NG",
        "DRIVERS_LICENSE",
        requiredFields = listOf(
            InputField.IdNumber,
            InputField.FirstName,
            InputField.LastName,
            InputField.Dob,
        ),
    ),
    NigeriaNINV2("NG", "NIN_V2"),
    NigeriaNINSlip("NG", "NIN_SLIP"),
    NigeriaVNIN("NG", "V_NIN"),
    NigeriaPhoneNumber(
        "NG",
        "PHONE_NUMBER",
        supportsEnhancedKyc = false,
        supportsBiometricKyc = false,
        // TODO: Check if phone_number field is explicitly required,
        //  or if ID Number *is* the phone number
    ),
    NigeriaVoterId("NG", "VOTER_ID"),

    SouthAfricaNationalId(
        "ZA",
        "NATIONAL_ID",
        supportsBasicKyc = false,
    ),
    SouthAfricaNationalIdNoPhoto(
        "ZA",
        "NATIONAL_ID_NO_PHOTO",
        supportsBiometricKyc = false,
    ),

    UgandaNationalIdNoPhoto("UG", "NATIONAL_ID_NO_PHOTO"),
    ;

    enum class InputField {
        IdNumber,
        FirstName,
        LastName,
        Dob,
        BankCode,
    }
}
