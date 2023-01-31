package com.smileidentity.networking

import com.smileidentity.networking.models.AuthenticationRequest
import com.smileidentity.networking.models.AuthenticationResponse
import com.smileidentity.networking.models.JobStatusRequest
import com.smileidentity.networking.models.JobStatusResponse
import com.smileidentity.networking.models.PrepUploadRequest
import com.smileidentity.networking.models.PrepUploadResponse
import com.smileidentity.networking.models.UploadRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

@Suppress("unused")
interface SmileIdentityService {
    @POST("/v1/auth_smile")
    suspend fun authenticate(@Body request: AuthenticationRequest): AuthenticationResponse

    @POST("/v1/upload")
    suspend fun prepUpload(@Body request: PrepUploadRequest): PrepUploadResponse

    @PUT
    suspend fun upload(@Url url: String, @Body request: UploadRequest)

    @POST("/v1/job_status")
    suspend fun getJobStatus(@Body request: JobStatusRequest): JobStatusResponse
}
