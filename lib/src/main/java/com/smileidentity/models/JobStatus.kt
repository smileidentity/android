@file:Suppress("unused")

package com.smileidentity.models

import android.os.Parcelable
import com.serjltt.moshi.adapters.FallbackEnum
import com.smileidentity.SmileID
import com.smileidentity.networking.calculateSignature
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
@JsonClass(generateAdapter = true)
data class JobStatusRequest(
    @Json(name = "user_id") val userId: String,
    @Json(name = "job_id") val jobId: String,
    @Json(name = "image_links") val includeImageLinks: Boolean,
    @Json(name = "history") val includeHistory: Boolean,
    @Json(name = "partner_id") val partnerId: String = SmileID.config.partnerId,
    @Json(name = "timestamp") val timestamp: String = System.currentTimeMillis().toString(),
    @Json(name = "signature") val signature: String = calculateSignature(timestamp),
) : Parcelable

interface JobStatusResponse : Parcelable {
    val timestamp: String
    val jobComplete: Boolean
    val jobSuccess: Boolean
    val code: String
    val result: JobResult?
    val history: List<JobResult.Entry>?
    val imageLinks: ImageLinks?
}

@Parcelize
@JsonClass(generateAdapter = true)
data class SmartSelfieJobStatusResponse(
    @Json(name = "timestamp") override val timestamp: String,
    @Json(name = "job_complete") override val jobComplete: Boolean,
    @Json(name = "job_success") override val jobSuccess: Boolean,
    @Json(name = "code") override val code: String,
    @Json(name = "result") override val result: SmartSelfieJobResult?,
    @Json(name = "history") override val history: List<SmartSelfieJobResult.Entry>?,
    @Json(name = "image_links") override val imageLinks: ImageLinks?,
) : JobStatusResponse

@Parcelize
@JsonClass(generateAdapter = true)
data class BiometricKycJobStatusResponse(
    @Json(name = "timestamp") override val timestamp: String,
    @Json(name = "job_complete") override val jobComplete: Boolean,
    @Json(name = "job_success") override val jobSuccess: Boolean,
    @Json(name = "code") override val code: String,
    @Json(name = "result") override val result: BiometricKycJobResult?,
    @Json(name = "history") override val history: List<BiometricKycJobResult.Entry>?,
    @Json(name = "image_links") override val imageLinks: ImageLinks?,
) : JobStatusResponse

@Parcelize
@JsonClass(generateAdapter = true)
data class DocumentVerificationJobStatusResponse(
    @Json(name = "timestamp") override val timestamp: String,
    @Json(name = "job_complete") override val jobComplete: Boolean,
    @Json(name = "job_success") override val jobSuccess: Boolean,
    @Json(name = "code") override val code: String,
    @Json(name = "result") override val result: DocumentVerificationJobResult?,
    @Json(name = "history") override val history: List<DocumentVerificationJobResult.Entry>?,
    @Json(name = "image_links") override val imageLinks: ImageLinks?,
) : JobStatusResponse

@Parcelize
@JsonClass(generateAdapter = true)
data class EnhancedDocumentVerificationJobStatusResponse(
    @Json(name = "timestamp") override val timestamp: String,
    @Json(name = "job_complete") override val jobComplete: Boolean,
    @Json(name = "job_success") override val jobSuccess: Boolean,
    @Json(name = "code") override val code: String,
    @Json(name = "result") override val result: EnhancedDocumentVerificationJobResult?,
    @Json(name = "history") override val history: List<DocumentVerificationJobResult.Entry>?,
    @Json(name = "image_links") override val imageLinks: ImageLinks?,
) : JobStatusResponse

interface JobResult : Parcelable {
    /**
     * The job result might sometimes be a freeform text field instead of an object (i.e. when the
     * zip upload has not finished processing on the backend, in which case "No zip uploaded" is
     * returned).
     */

    @JvmInline
    @Parcelize
    value class Freeform(
        val result: String,
    ) : SmartSelfieJobResult,
        DocumentVerificationJobResult,
        BiometricKycJobResult,
        EnhancedDocumentVerificationJobResult

    interface Entry : JobResult {
        val actions: Actions
        val resultCode: String
        val resultText: String
        val smileJobId: String
        val partnerParams: PartnerParams
    }
}

sealed interface SmartSelfieJobResult : JobResult {
    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Entry(
        @Json(name = "Actions") override val actions: Actions,
        @Json(name = "ResultCode") override val resultCode: String,
        @Json(name = "ResultText") override val resultText: String,
        @Json(name = "SmileJobID") override val smileJobId: String,
        @Json(name = "PartnerParams") override val partnerParams: PartnerParams,
        @Json(name = "ConfidenceValue") val confidence: Double?,
    ) : SmartSelfieJobResult, JobResult.Entry
}

sealed interface DocumentVerificationJobResult : JobResult {
    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Entry(
        @Json(name = "Actions") override val actions: Actions,
        @Json(name = "ResultCode") override val resultCode: String,
        @Json(name = "ResultText") override val resultText: String,
        @Json(name = "SmileJobID") override val smileJobId: String,
        @Json(name = "PartnerParams") override val partnerParams: PartnerParams,
        @Json(name = "Country") val country: String?,
        @Json(name = "IDType") val idType: String?,
        @Json(name = "IDNumber") val idNumber: String?,
        @Json(name = "FullName") val fullName: String?,
        @Json(name = "DOB") val dob: String?,
        @Json(name = "Gender") val gender: String?,
        @Json(name = "ExpirationDate") val expirationDate: String?,
        @Json(name = "Document") val documentImageBase64: String?,
        @Json(name = "PhoneNumber") val phoneNumber: String?,
        @Json(name = "PhoneNumber2") val phoneNumber2: String?,
        @Json(name = "Address") val address: String?,
    ) : DocumentVerificationJobResult, JobResult.Entry
}

sealed interface BiometricKycJobResult : JobResult {
    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Entry(
        @Json(name = "Actions") override val actions: Actions,
        @Json(name = "ResultCode") override val resultCode: String,
        @Json(name = "ResultText") override val resultText: String,
        @Json(name = "ResultType") val resultType: String,
        @Json(name = "SmileJobID") override val smileJobId: String,
        @Json(name = "PartnerParams") override val partnerParams: PartnerParams,
        @Json(name = "Antifraud") val antifraud: Antifraud?,
        @Json(name = "DOB") val dob: String?,
        @Json(name = "Photo") val photoBase64: String?,
        @Json(name = "Gender") val gender: String?,
        @Json(name = "IDType") val idType: String?,
        @Json(name = "Address") val address: String?,
        @Json(name = "Country") val country: String?,
        @Json(name = "Document") val documentImageBase64: String?,
        @Json(name = "FullData") val fullData: @RawValue Map<String, Any>?,
        @Json(name = "FullName") val fullName: String?,
        @Json(name = "IDNumber") val idNumber: String?,
        @Json(name = "PhoneNumber") val phoneNumber: String?,
        @Json(name = "PhoneNumber2") val phoneNumber2: String?,
        @Json(name = "ExpirationDate") val expirationDate: String?,
        @Json(name = "Secondary_ID_Number") val secondaryIdNumber: String?,
        @Json(name = "IDNumberPreviouslyRegistered") val idNumberPreviouslyRegistered: Boolean?,
        @Json(name = "UserIDsOfPreviousRegistrants") val previousRegistrantsUserIds: List<String>?,
    ) : BiometricKycJobResult, JobResult.Entry
}

sealed interface EnhancedDocumentVerificationJobResult : JobResult {
    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Entry(
        @Json(name = "Actions") override val actions: Actions,
        @Json(name = "ResultCode") override val resultCode: String,
        @Json(name = "ResultText") override val resultText: String,
        @Json(name = "ResultType") val resultType: String,
        @Json(name = "SmileJobID") override val smileJobId: String,
        @Json(name = "PartnerParams") override val partnerParams: PartnerParams,
        @Json(name = "Antifraud") val antifraud: Antifraud?,
        @Json(name = "DOB") val dob: String?,
        @Json(name = "Photo") val photoBase64: String?,
        @Json(name = "Gender") val gender: String?,
        @Json(name = "IDType") val idType: String?,
        @Json(name = "Address") val address: String?,
        @Json(name = "Country") val country: String?,
        @Json(name = "Document") val documentImageBase64: String?,
        @Json(name = "FullData") val fullData: @RawValue Map<String, Any>?,
        @Json(name = "FullName") val fullName: String?,
        @Json(name = "IDNumber") val idNumber: String?,
        @Json(name = "PhoneNumber") val phoneNumber: String?,
        @Json(name = "PhoneNumber2") val phoneNumber2: String?,
        @Json(name = "ExpirationDate") val expirationDate: String?,
        @Json(name = "Secondary_ID_Number") val secondaryIdNumber: String?,
        @Json(name = "IDNumberPreviouslyRegistered") val idNumberPreviouslyRegistered: Boolean?,
        @Json(name = "UserIDsOfPreviousRegistrants") val previousRegistrantsUserIds: List<String>?,
    ) : EnhancedDocumentVerificationJobResult, JobResult.Entry
}

@Parcelize
@JsonClass(generateAdapter = true)
data class Antifraud(
    @Json(name = "SuspectUsers") val suspectUsers: List<SuspectUser>,
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class SuspectUser(
    @Json(name = "reason") val reason: String,
    @Json(name = "user_id") val userId: String,
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Actions(
    @Json(name = "Document_Check")
    val documentCheck: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Human_Review_Compare")
    val humanReviewCompare: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Human_Review_Document_Check")
    val humanReviewDocumentCheck: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Human_Review_Liveness_Check")
    val humanReviewLivenessCheck: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Human_Review_Selfie_Check")
    val humanReviewSelfieCheck: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Human_Review_Update_Selfie")
    val humanReviewUpdateSelfie: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Liveness_Check")
    val livenessCheck: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Register_Selfie")
    val registerSelfie: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Return_Personal_Info")
    val returnPersonalInfo: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Selfie_Check")
    val selfieCheck: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Selfie_Provided")
    val selfieProvided: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Selfie_To_ID_Authority_Compare")
    val selfieToIdAuthorityCompare: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Selfie_To_ID_Card_Compare")
    val selfieToIdCardCompare: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Selfie_To_Registered_Selfie_Compare")
    val selfieToRegisteredSelfieCompare: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Update_Registered_Selfie_On_File")
    val updateRegisteredSelfieOnFile: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Verify_Document")
    val verifyDocument: ActionResult = ActionResult.NotApplicable,

    @Json(name = "Verify_ID_Number")
    val verifyIdNumber: ActionResult = ActionResult.NotApplicable,
) : Parcelable

@FallbackEnum(name = "Unknown")
enum class ActionResult {
    @Json(name = "Passed")
    Passed,

    @Json(name = "Completed")
    Completed,

    @Json(name = "Approved")
    Approved,

    @Json(name = "Verified")
    Verified,

    @Json(name = "Provisionally Approved")
    ProvisionallyApproved,

    @Json(name = "Returned")
    Returned,

    @Json(name = "Not Returned")
    NotReturned,

    @Json(name = "Failed")
    Failed,

    @Json(name = "Rejected")
    Rejected,

    @Json(name = "Under Review")
    UnderReview,

    @Json(name = "Unable To Determine")
    UnableToDetermine,

    @Json(name = "Not Applicable")
    NotApplicable,

    @Json(name = "Not Verified")
    NotVerified,

    @Json(name = "Not Done")
    NotDone,

    @Json(name = "Issuer Unavailable")
    IssuerUnavailable,

    @Json(name = "ID Authority Photo Not Available")
    IdAuthorityPhotoNotAvailable,

    @Json(name = "Sent to Human Review")
    SentToHumanReview,

    /**
     * Special value used to indicate that the value returned from the server is not yet supported
     * by the SDK. Please update the SDK to the latest version to support the latest values.
     */
    Unknown,
}

@Parcelize
@JsonClass(generateAdapter = true)
data class ImageLinks(
    @Json(name = "selfie_image") val selfieImageUrl: String?,
    @Json(name = "error") val error: String?,
) : Parcelable
