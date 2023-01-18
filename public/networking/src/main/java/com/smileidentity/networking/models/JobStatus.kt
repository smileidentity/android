@file:Suppress("unused")

package com.smileidentity.networking.models

import com.smileidentity.networking.SmileIdentity
import com.smileidentity.networking.StringifiedBoolean
import com.smileidentity.networking.calculateSignature
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JobStatusRequest(
    @Json(name = "user_id") val userId: String,
    @Json(name = "job_id") val jobId: String,
    @Json(name = "image_links") val includeImageLinks: Boolean,
    @Json(name = "history") val includeHistory: Boolean,
    @Json(name = "partner_id") val partnerId: String = SmileIdentity.config.partnerId,
    @Json(name = "timestamp") val timestamp: String = System.currentTimeMillis().toString(),
    @Json(name = "signature") val signature: String = calculateSignature(timestamp),
)

@JsonClass(generateAdapter = true)
data class JobStatusResponse(
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "signature") val signature: String,
    @Json(name = "job_complete") val jobComplete: Boolean,
    @Json(name = "job_success") val jobSuccess: Boolean,
    @Json(name = "code") val code: Int,
    @Json(name = "result") val result: JobResult?,
    @Json(name = "history") val history: List<JobResult.Entry>?,
    @Json(name = "image_links") val imageLinks: ImageLinks?,
)

/**
 * The job result might sometimes be a freeform text field instead of an object (i.e. when the
 * zip upload has not finished processing on the backend, in which case "No zip uploaded" is
 * returned).
 */
sealed interface JobResult {
    data class Freeform(val result: String) : JobResult

    @JsonClass(generateAdapter = true)
    data class Entry(
        @Json(name = "Source") val source: String,
        @Json(name = "Actions") val actions: Actions,
        @Json(name = "ResultCode") val resultCode: Int,
        @Json(name = "ResultText") val resultText: String,
        @Json(name = "ResultType") val resultType: String,
        @Json(name = "SmileJobID") val smileJobId: String,
        @Json(name = "JSONVersion") val jsonVersion: String,
        @Json(name = "PartnerParams") val partnerParams: PartnerParams,
        @Json(name = "ConfidenceValue") val confidence: Int,

        @Json(name = "IsFinalResult") @StringifiedBoolean
        val isFinalResult: Boolean,

        @Json(name = "IsMachineResult") @StringifiedBoolean
        val isMachineResult: Boolean,
    ) : JobResult
}

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
    @Json(name = "Passed")
    Passed,

    @Json(name = "Approved")
    Approved,

    @Json(name = "Provisionally Approved")
    ProvisionallyApproved,

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
}

@JsonClass(generateAdapter = true)
data class ImageLinks(
    @Json(name = "selfie_image") val selfieImageUrl: String?,
    @Json(name = "error") val error: String?,
)
