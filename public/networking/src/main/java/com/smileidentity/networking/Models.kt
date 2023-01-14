@file:Suppress("unused")

package com.smileidentity.networking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class Config(
    @Json(name = "test_url") val testUrl: String,
    @Json(name = "test_lambda_url") val testLambdaUrl: String,
    @Json(name = "prod_url") val prodUrl: String,
    @Json(name = "prod_lambda_url") val prodLambdaUrl: String,
    @Json(name = "auth_token") val authToken: String,
    @Json(name = "partner_id") val partnerId: String,
    @Json(name = "version") val version: String,
)

@JsonClass(generateAdapter = true)
data class RegisterUserRequest(
    @Json(name = "source_sdk") val sourceSdk: String = "android",
    @Json(name = "source_sdk_version") val sourceSdkVersion: String = "2.0.0",
    @Json(name = "file_name") val filename: String = "upload.zip",
    @Json(name = "signature") val signature: String,
    @Json(name = "timestamp") val timestamp: String, // TODO: Date
    @Json(name = "smile_client_id") val partnerId: String,
    @Json(name = "partner_params") val partnerParams: PartnerParams,
    @Json(name = "model_parameters") val modelParameters: Map<String, Any> = mapOf(),
    // Callback URL *must* be defined either within your Partner Portal or here
    @Json(name = "callback_url") val callbackUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class PartnerParams(
    @Json(name = "job_type") val jobType: JobType,
    @Json(name = "job_id") val jobId: String,
    @Json(name = "user_id") val userId: String,
)

enum class JobType {
    @Json(name = "4") SmartSelfieEnrollment,
}

@JsonClass(generateAdapter = true)
data class RegisterUserResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "ref_id") val refId: String,
    @Json(name = "upload_url") val uploadUrl: String,
    @Json(name = "smile_job_id") val smileJobId: String,
    @Json(name = "camera_config") val cameraConfig: String?,
)

@JsonClass(generateAdapter = true)
data class UploadResponse(
    @Json(name = "SmileJobID") val jobId: String,
    @Json(name = "ResultCode") val resultCode: Int,
    @Json(name = "ResultText") val resultText: String,
    @Json(name = "ConfidenceValue") val confidence: Int,
    @Json(name = "timestamp") val timestamp: String, // todo: date
    @Json(name = "Source") val source: String,
    @Json(name = "signature") val signature: String,
    @Json(name = "PartnerParams") val partnerParams: PartnerParams,
    @Json(name = "Actions") val actions: Actions,
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
data class SmileIdentityError(
    @Json(name = "code") val code: Int,
    @Json(name = "error") val message: String,
)

@JsonClass(generateAdapter = true)
data class UploadRequest(
    @Json(name = "images") val images: List<UploadImageInfo>,
    @Json(name = "package_information") val packageInformation: UploadPackageInformation = UploadPackageInformation(),
)

@JsonClass(generateAdapter = true)
data class UploadPackageInformation(
    @Json(name = "apiVersion") val apiVersion: ApiVersion = ApiVersion(),
)

@JsonClass(generateAdapter = true)
data class ApiVersion(
    @Json(name = "buildNumber") val buildNumber: Int = 0,
    @Json(name = "majorVersion") val majorVersion: Int = 2,
    @Json(name = "minorVersion") val minorVersion: Int = 0,
)

@JsonClass(generateAdapter = true)
data class UploadImageInfo(
    @Json(name = "image_type_id") val imageTypeId: ImageType,
    @Json(name = "file_name") val image: File,
)

enum class ImageType {
    @Json(name = "0") SelfiePngOrJpgFile,
    @Json(name = "1") IdCardPngOrJpgFile,
    @Json(name = "2") SelfiePngOrJpgBase64,
    @Json(name = "3") IdCardPngOrJpgBase64,
    @Json(name = "4") LivenessPngOrJpgFile,
    @Json(name = "5") IdCardRearPngOrJpgFile,
    @Json(name = "6") LivenessPngOrJpgBase64,
    @Json(name = "7") IdCardRearPngOrJpgBase64,
}

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
data class ImageLinks(
    @Json(name = "selfie_image") val selfieImageUrl: String,
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
