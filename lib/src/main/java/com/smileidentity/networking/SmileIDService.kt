package com.smileidentity.networking

import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.DocVJobStatusResponse
import com.smileidentity.models.EnhancedKycRequest
import com.smileidentity.models.EnhancedKycResponse
import com.smileidentity.models.JobStatusRequest
import com.smileidentity.models.JobStatusResponse
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.PrepUploadResponse
import com.smileidentity.models.ProductsConfigRequest
import com.smileidentity.models.ProductsConfigResponse
import com.smileidentity.models.ServicesResponse
import com.smileidentity.models.UploadRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

@Suppress("unused")
interface SmileIDService {
    /**
     * Returns a signature and timestamp that can be used to authenticate future requests. This is
     * necessary only when using the [com.smileidentity.models.Config.authToken] and
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

    /**
     * Fetches the status of a Job. This can be used to check if a Job is complete, and if so,
     * whether it was successful. This should be called when the Job is known to be a
     * SmartSelfie Authentication/Registration.
     */
    @POST("/v1/job_status")
    suspend fun getJobStatus(@Body request: JobStatusRequest): JobStatusResponse

    /**
     * Fetches the status of a Job. This can be used to check if a Job is complete, and if so,
     * whether it was successful. This should be called when the Job is known to be a
     * Document Verification.
     */
    @POST("/v1/job_status")
    suspend fun getDocVJobStatus(@Body request: JobStatusRequest): DocVJobStatusResponse

    /**
     * Returns the ID types that are enabled for authenticated partner and which of those require
     * consent
     */
    @POST("/v1/products_config")
    suspend fun getProductsConfig(@Body request: ProductsConfigRequest): ProductsConfigResponse

    @GET("/v1/services")
    suspend fun getServices(): ServicesResponse
}
