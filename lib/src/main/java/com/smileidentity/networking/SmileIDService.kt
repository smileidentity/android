@file:Suppress("unused")

package com.smileidentity.networking

import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.smileidentity.SmileIDOptIn
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.BiometricKycJobStatusResponse
import com.smileidentity.models.BvnTotpModeRequest
import com.smileidentity.models.BvnTotpModeResponse
import com.smileidentity.models.BvnTotpRequest
import com.smileidentity.models.BvnTotpResponse
import com.smileidentity.models.DocumentVerificationJobStatusResponse
import com.smileidentity.models.EnhancedDocumentVerificationJobStatusResponse
import com.smileidentity.models.EnhancedKycAsyncResponse
import com.smileidentity.models.EnhancedKycRequest
import com.smileidentity.models.EnhancedKycResponse
import com.smileidentity.models.JobStatusRequest
import com.smileidentity.models.JobStatusResponse
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.PrepUploadResponse
import com.smileidentity.models.ProductsConfigRequest
import com.smileidentity.models.ProductsConfigResponse
import com.smileidentity.models.ServicesResponse
import com.smileidentity.models.SmartSelfieJobResult
import com.smileidentity.models.SmartSelfieJobStatusResponse
import com.smileidentity.models.SubmitBvnTotpRequest
import com.smileidentity.models.SubmitBvnTotpResponse
import com.smileidentity.models.UploadRequest
import com.smileidentity.models.ValidDocumentsResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Url

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

    // TODO: Once the API no longer requires the filename to be sent, change selfieImage and
    //  livenessImages to be List<File> and use a File to RequestBody converter instead. This will
    //  allow us to specify the Part name on the API/service definition rather than when creating
    //  the request body.
    @Multipart
    @POST("/v1/biometric_authentication")
    @SmileIDOptIn
    suspend fun doBiometricAuthentication(
        @Header("SmileID-Timestamp") timestamp: String,
        @Header("SmileID-Request-Signature") signature: String,
        @Header("SmileID-Partner-ID") partnerId: String = SmileID.config.partnerId,
        @Part selfieImage: MultipartBody.Part,
        @Part livenessImages: List<@JvmSuppressWildcards MultipartBody.Part>,
        @Part("partner_params") partnerParams: PartnerParams,
        @Part("source_sdk") sourceSdk: String = "android",
        @Part("source_sdk_version") sourceSdkVersion: String = BuildConfig.VERSION_NAME,
    ): SmartSelfieJobResult.Entry

    /**
     * Query the Identity Information of an individual using their ID number from a supported ID
     * Type. Return the personal information of the individual found in the database of the ID
     * authority.
     *
     * This will be done synchronously, and the result will be returned in the response. If the ID
     * provider is unavailable, the response will be an error.
     */
    @POST("/v1/id_verification")
    suspend fun doEnhancedKyc(@Body request: EnhancedKycRequest): EnhancedKycResponse

    /**
     * Same as [doEnhancedKyc], but the final result is delivered the URL provided in the (required)
     * [EnhancedKycRequest.callbackUrl] field.
     *
     * If the ID provider is unavailable, the response will be delivered to the callback URL once
     * the ID provider is available again.
     */
    @POST("/v1/async_id_verification")
    suspend fun doEnhancedKycAsync(@Body request: EnhancedKycRequest): EnhancedKycAsyncResponse

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
    suspend fun getDocumentVerificationJobStatus(
        @Body request: JobStatusRequest,
    ): DocumentVerificationJobStatusResponse

    /**
     * Fetches the status of a Job. This can be used to check if a Job is complete, and if so,
     * whether it was successful. This should be called when the Job is known to be a Biometric KYC.
     */
    @POST("/v1/job_status")
    suspend fun getBiometricKycJobStatus(
        @Body request: JobStatusRequest,
    ): BiometricKycJobStatusResponse

    /**
     * Fetches the status of a Job. This can be used to check if a Job is complete, and if so,
     * whether it was successful. This should be called when the Job is known to be Enhanced DocV.
     */
    @POST("/v1/job_status")
    suspend fun getEnhancedDocumentVerificationJobStatus(
        @Body request: JobStatusRequest,
    ): EnhancedDocumentVerificationJobStatusResponse

    /**
     * Returns the ID types that are enabled for authenticated partner and which of those require
     * consent
     */
    @POST("/v1/products_config")
    suspend fun getProductsConfig(@Body request: ProductsConfigRequest): ProductsConfigResponse

    /**
     * Returns Global DocV supported products and metadata
     */
    @POST("/v1/valid_documents")
    suspend fun getValidDocuments(@Body request: ProductsConfigRequest): ValidDocumentsResponse

    /**
     * Returns supported products and metadata
     */
    @GET("/v1/services")
    suspend fun getServices(): ServicesResponse

    /**
     * Returns the different modes of getting the BVN OTP, either via sms or email
     */
    @POST("/v1/totp_consent")
    suspend fun requestBvnTotpMode(@Body request: BvnTotpRequest): BvnTotpResponse

    /**
     * Returns the BVN OTP via the selected mode
     */
    @POST("/v1/totp_consent/mode")
    suspend fun requestBvnOtp(@Body request: BvnTotpModeRequest): BvnTotpModeResponse

    /**
     * Submits the BVN OTP for verification
     */
    @POST("/v1/totp_consent/otp")
    suspend fun submitBvnOtp(@Body request: SubmitBvnTotpRequest): SubmitBvnTotpResponse
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
fun SmileIDService.pollDocumentVerificationJobStatus(
    request: JobStatusRequest,
    interval: Duration = 1.seconds,
    numAttempts: Int = 30,
) = poll(interval, numAttempts) { getDocumentVerificationJobStatus(request) }

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
fun SmileIDService.pollEnhancedDocumentVerificationJobStatus(
    request: JobStatusRequest,
    interval: Duration = 1.seconds,
    numAttempts: Int = 30,
) = poll(interval, numAttempts) { getEnhancedDocumentVerificationJobStatus(request) }

/**
 * This uses a generics (as compared to the interface as the return type of [action] directly) so
 * that the higher level callers (defined above) have a concrete return type
 *
 * [channelFlow] is used instead of [kotlinx.coroutines.flow.flow] so that API calls continue to be
 * made when the consumer processes slower than the producer
 *
 * It is recommended to collect this flow with [kotlinx.coroutines.flow.collectLatest] (note: if
 * consuming slower than this is producing, the consumer coroutine will continue getting cancelled
 * until the last value) since the flow will complete when the job is complete
 *
 * Alternatively, [kotlinx.coroutines.flow.collect] can be used along with
 * [kotlinx.coroutines.flow.conflate] to drop older, non-consumed values when newer values are
 * present
 */
internal fun <T : JobStatusResponse> poll(
    interval: Duration,
    numAttempts: Int,
    action: suspend (attempt: Int) -> T,
) = channelFlow {
    var latestError: Exception? = null
    for (attempt in 0..<numAttempts) {
        try {
            val response = action(attempt)
            send(response)

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
