package com.smileidentity.networking

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
    @POST("/v1/upload")
    fun registerUser(@Body request: PrepUploadRequest): SmileIdentityCall<PrepUploadResponse>

    @PUT
    fun upload(@Url url: String, @Body request: UploadRequest): SmileIdentityCall<Unit>

    @POST("/v1/job_status")
    fun getJobStatus(@Body request: JobStatusRequest): SmileIdentityCall<JobStatusResponse>
}
