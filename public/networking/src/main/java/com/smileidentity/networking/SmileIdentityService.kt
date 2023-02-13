package com.smileidentity.networking

import com.smileidentity.networking.models.AuthenticationRequest
import com.smileidentity.networking.models.AuthenticationResponse
import com.smileidentity.networking.models.EnhancedKycRequest
import com.smileidentity.networking.models.EnhancedKycResponse
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
    /**
     * Returns a signature and timestamp that can be used to authenticate future requests. This is
     * necessary only when using the [com.smileidentity.networking.models.Config.authToken] and
     * *not* using the API key.
     */
    @POST("/v1/auth_smile")
    suspend fun authenticate(@Body request: AuthenticationRequest): AuthenticationResponse

    /**
     * Used by Job Types that need to upload a file to the server. The response contains the URL
     * [PrepUploadResponse.uploadUrl] that the file should eventually be uploaded to (via [upload]).
     */
    @POST("/v1/upload")
    suspend fun prepUpload(@Body request: PrepUploadRequest): PrepUploadResponse

    /**
     * Uploads files to S3. The URL should be the one returned by [prepUpload]. The files will be
     * uploaded in the order they are provided in [UploadRequest.images], and will be zipped
     * together by [UploadRequestConverterFactory] and [FileAdapter]
     */
    @PUT
    suspend fun upload(@Url url: String, @Body request: UploadRequest)

    /**
     * Query the Identity Information of an individual using their ID number from a supported ID
     * Type. Return the personal information of the individual found in the database of the ID
     * authority.
     */
    @POST("/v1/id_verification")
    suspend fun doEnhancedKyc(@Body request: EnhancedKycRequest): EnhancedKycResponse

    @POST("/v1/job_status")
    suspend fun getJobStatus(@Body request: JobStatusRequest): JobStatusResponse
}
