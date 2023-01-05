package com.smileidentity.networking

import retrofit2.http.POST

interface SmileIdentityService {
    @POST("/v1/upload")
    fun registerUser(registerUserRequest: RegisterUserRequest): RegisterUserResponse
}

data class RegisterUserRequest(
    val sourceSdk: String = "android",
    val sourceSdkVersion: String = "1.0.0",
    val filename: String = "upload.zip",
    val signature: String,
    val timestamp: String,
    val partnerId: String, // TODO: This should be (de)serialized as "smile_client_id"
    val partnerParams: PartnerParams,
    val modelParameters: Map<String, Any> = mapOf(),
    val callbackUrl: String? = null,
)


data class PartnerParams(
    val jobType: JobType,
    val jobId: String,
    val userId: String,
)

enum class JobType(val value: String) {
    Selfie("4"),
}


data class RegisterUserResponse(
    val uploadUrl: String,
    val refId: String,
    val smileJobId: String,
    val cameraConfig: String,
    val code: String,
)
