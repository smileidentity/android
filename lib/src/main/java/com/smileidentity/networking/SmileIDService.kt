@file:Suppress("unused")

package com.smileidentity.networking

import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.BiometricKycJobStatusResponse
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
import com.smileidentity.models.SmartSelfieJobStatusResponse
import com.smileidentity.models.UploadRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
    suspend fun getSmartSelfieJobStatus(
        @Body request: JobStatusRequest,
    ): SmartSelfieJobStatusResponse

    /**
     * Fetches the status of a Job. This can be used to check if a Job is complete, and if so,
     * whether it was successful. This should be called when the Job is known to be a
     * Document Verification.
     */
    @POST("/v1/job_status")
    suspend fun getDocVJobStatus(@Body request: JobStatusRequest): DocVJobStatusResponse

    /**
     * Fetches the status of a Job. This can be used to check if a Job is complete, and if so,
     * whether it was successful. This should be called when the Job is known to be a Biometric KYC.
     */
    @POST("/v1/job_status")
    suspend fun getBiometricKycJobStatus(
        @Body request: JobStatusRequest,
    ): BiometricKycJobStatusResponse

    /**
     * Returns the ID types that are enabled for authenticated partner and which of those require
     * consent
     */
    @POST("/v1/products_config")
    suspend fun getProductsConfig(@Body request: ProductsConfigRequest): ProductsConfigResponse

    @GET("/v1/services")
    suspend fun getServices(): ServicesResponse
}

/**
 * Polls the server for the status of a Job until it is complete. This should be called after the
 * Job has been submitted to the server. The returned flow will be updated with every job status
 * response. The flow will complete when the job is complete, or the attempt limit is reached.
 * If any exceptions occur, only the last one will be thrown. If there is a successful API response
 * after an exception, the exception will be ignored.
 *
 * @param request The [JobStatusRequest] to make to the server
 * @param interval The interval between each poll
 * @param numAttempts The number of times to poll before giving up
 */
fun SmileIDService.pollSmartSelfieJobStatus(
    request: JobStatusRequest,
    interval: Duration = 1.seconds,
    numAttempts: Int = 30,
) = poll(interval, numAttempts) { getSmartSelfieJobStatus(request) }

/**
 * Polls the server for the status of a Job until it is complete. This should be called after the
 * Job has been submitted to the server. The returned flow will be updated with every job status
 * response. The flow will complete when the job is complete, or the attempt limit is reached.
 * If any exceptions occur, only the last one will be thrown. If there is a successful API response
 * after an exception, the exception will be ignored.
 *
 * @param request The [JobStatusRequest] to make to the server
 * @param interval The interval between each poll
 * @param numAttempts The number of times to poll before giving up
 */
fun SmileIDService.pollDocVJobStatus(
    request: JobStatusRequest,
    interval: Duration = 1.seconds,
    numAttempts: Int = 30,
) = poll(interval, numAttempts) { getDocVJobStatus(request) }

/**
 * Polls the server for the status of a Job until it is complete. This should be called after the
 * Job has been submitted to the server. The returned flow will be updated with every job status
 * response. The flow will complete when the job is complete, or the attempt limit is reached.
 * If any exceptions occur, only the last one will be thrown. If there is a successful API response
 * after an exception, the exception will be ignored.
 *
 * @param request The [JobStatusRequest] to make to the server
 * @param interval The interval between each poll
 * @param numAttempts The number of times to poll before giving up
 */
fun SmileIDService.pollBiometricKycJobStatus(
    request: JobStatusRequest,
    interval: Duration = 1.seconds,
    numAttempts: Int = 30,
) = poll(interval, numAttempts) { getBiometricKycJobStatus(request) }

/**
 * This uses a generics (as compared to the interface as the return type of [action] directly) so
 * that the higher level callers (defined above) have a concrete return type
 */
internal fun <T : JobStatusResponse> poll(
    interval: Duration,
    numAttempts: Int,
    action: suspend (attempt: Int) -> T,
) = flow {
    var latestError: Exception? = null
    // TODO: Replace `until` with `..<` once ktlint-gradle plugin stops throwing an exception for it
    //  see: https://github.com/JLLeitschuh/ktlint-gradle/issues/692
    for (attempt in 0 until numAttempts) {
        try {
            val response = action(attempt)
            emit(response)

            // Reset the error if the API response was successful
            latestError = null

            if (response.jobComplete) {
                break
            }
        } catch (e: Exception) {
            latestError = e
        }
        delay(interval)
    }
    latestError?.let { throw it }
}
