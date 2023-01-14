package com.smileidentity.networking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JobStatusRequest(
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "signature") val signature: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "job_id") val jobId: String,
    @Json(name = "partner_id") val partnerId: String,
    @Json(name = "image_links") val includeImageLinks: Boolean,
    @Json(name = "history") val includeHistory: Boolean,
)
@JsonClass(generateAdapter = true)
data class JobStatusResponse(
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "signature") val signature: String,
    @Json(name = "job_complete") val jobComplete: Boolean,
    @Json(name = "job_success") val jobSuccess: Boolean,
    @Json(name = "code") val code: Int,
    @Json(name = "result") val result: JobResultEntry?,
    @Json(name = "history") val history: List<JobResultEntry>?,
    @Json(name = "image_links") val imageLinks: ImageLinks?,
)

@JsonClass(generateAdapter = true)
data class JobResultEntry(
    @Json(name = "Source") val source: String,
    @Json(name = "Actions") val actions: Actions,
    @Json(name = "ResultCode") val resultCode: Int,
    @Json(name = "ResultText") val resultText: String,
    @Json(name = "ResultType") val resultType: String,
    @Json(name = "SmileJobID") val smileJobId: String,
    @Json(name = "JSONVersion") val jsonVersion: String,
    @StringifiedBoolean @Json(name = "IsFinalResult") val isFinalResult: Boolean,
    @Json(name = "ConfidenceValue") val confidence: Int,
    @StringifiedBoolean @Json(name = "IsMachineResult") val isMachineResult: Boolean,
    @Json(name = "PartnerParams") val partnerParams: PartnerParams,
)

@JsonClass(generateAdapter = true)
data class Actions(
    @Json(name = "Human_Review_Compare") val humanReviewCompare: ActionResult,
    @Json(name = "Human_Review_Liveness_Check") val humanReviewLivenessCheck: ActionResult,
    @Json(name = "Human_Review_Selfie_Check") val humanReviewSelfieCheck: ActionResult,
    @Json(name = "Human_Review_Update_Selfie") val humanReviewUpdateSelfie: ActionResult,
    @Json(name = "Liveness_Check") val livenessCheck: ActionResult,
    @Json(name = "Selfie_Check") val selfieCheck: ActionResult,
    @Json(name = "Register_Selfie") val registerSelfie: ActionResult,
    @Json(name = "Return_Personal_Info") val returnPersonalInfo: ActionResult,
    @Json(name = "Selfie_Provided") val selfieProvided: ActionResult,
    @Json(name = "Selfie_To_ID_Authority_Compare") val selfieToIdAuthorityCompare: ActionResult,
    @Json(name = "Selfie_To_ID_Card_Compare") val selfieToIdCardCompare: ActionResult,
    @Json(name = "Selfie_To_Registered_Selfie_Compare") val selfieToRegisteredSelfieCompare: ActionResult,
    @Json(name = "Update_Registered_Selfie_On_File") val updateRegisteredSelfieOnFile: ActionResult,
    @Json(name = "Verify_ID_Number") val verifyIdNumber: ActionResult,
)

enum class ActionResult {
    @Json(name = "Passed") Passed,
    @Json(name = "Approved") Approved,
    @Json(name = "Provisionally Approved") ProvisionallyApproved,
    @Json(name = "Failed") Failed,
    @Json(name = "Rejected") Rejected,
    @Json(name = "Under Review") UnderReview,
    @Json(name = "Unable To Determine") UnableToDetermine,
    @Json(name = "Not Applicable") NotApplicable,
    Unknown,
}

@JsonClass(generateAdapter = true)
data class ImageLinks(
    @Json(name = "selfie_image") val selfieImageUrl: String,
)
