package com.smileidentity.models

import android.os.Parcelable
import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.smileidentity.models.v2.metadata.Metadatum
import com.smileidentity.networking.calculateSignature
import com.smileidentity.util.getCurrentIsoTimestamp
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class EnhancedKycRequest(
    @Json(name = "country") val country: String,
    @Json(name = "id_type") val idType: String,
    @Json(name = "id_number") val idNumber: String,
    @Json(name = "consent_information") val consentInformation: ConsentInformation =
        ConsentInformation(
            consentGrantedDate = getCurrentIsoTimestamp(),
            personalDetailsConsentGranted = false,
            contactInfoConsentGranted = false,
            documentInfoConsentGranted = false,
        ),
    @Json(name = "first_name") val firstName: String? = null,
    @Json(name = "middle_name") val middleName: String? = null,
    @Json(name = "last_name") val lastName: String? = null,
    @Json(name = "dob") val dob: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    @Json(name = "bank_code") val bankCode: String? = null,
    @Json(name = "callback_url") val callbackUrl: String? = SmileID.callbackUrl,
    @Json(name = "partner_params") val partnerParams: PartnerParams,
    @Json(name = "partner_id") val partnerId: String = SmileID.config.partnerId,
    @Json(name = "source_sdk") val sourceSdk: String = "android",
    @Json(name = "source_sdk_version") val sourceSdkVersion: String = BuildConfig.VERSION_NAME,
    @Json(name = "timestamp") val timestamp: String = getCurrentIsoTimestamp(),
    @Json(name = "signature") val signature: String = calculateSignature(timestamp),
    @Json(name = "metadata") val metadata: List<Metadatum>? = null,
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class EnhancedKycResponse(
    @Json(name = "SmileJobID") val smileJobId: String,
    @Json(name = "PartnerParams") val partnerParams: PartnerParams,
    @Json(name = "ResultText") val resultText: String,
    @Json(name = "ResultCode") val resultCode: String,
    @Json(name = "Actions") val actions: Actions,
    @Json(name = "Country") val country: String,
    @Json(name = "IDType") val idType: String,
    @Json(name = "IDNumber") val idNumber: String,
    @Json(name = "FullName") val fullName: String?,
    @Json(name = "ExpirationDate") val expirationDate: String?,
    @Json(name = "DOB") val dob: String?,
    @Json(name = "Photo") val base64Photo: String?,
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class EnhancedKycAsyncResponse(@Json(name = "success") val success: Boolean) : Parcelable
