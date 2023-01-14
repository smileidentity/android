package com.smileidentity.networking

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

@Suppress("unused")
interface SmileIdentityService {
    @POST("/v1/upload")
    fun registerUser(@Body registerUserRequest: RegisterUserRequest): Call<RegisterUserResponse>

    @PUT
    fun upload(@Url url: String, @Body uploadRequest: UploadRequest): Call<Unit>

    @POST("/v1/job_status")
    fun getJobStatus(@Body jobStatusRequest: JobStatusRequest): Call<JobStatusResponse>
}
