package com.smileidentity.submissions.base

import android.os.Parcelable
import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.PrepUploadResponse
import com.smileidentity.models.UploadRequest
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.createAuthenticationRequestFile
import com.smileidentity.util.createPrepUploadFile
import com.smileidentity.util.toSmileIDException
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.withContext
import retrofit2.HttpException

abstract class BaseJobSubmission<T : Parcelable>(
    protected val jobId: String,
) {
    // abstract methods to be implemented by subclasses
    protected abstract fun createAuthRequest(): AuthenticationRequest
    protected abstract fun createPrepUploadRequest(
        authResponse: AuthenticationResponse? = null,
    ): PrepUploadRequest
    protected abstract fun createUploadRequest(authResponse: AuthenticationResponse?): UploadRequest
    protected abstract suspend fun createSuccessResult(didSubmit: Boolean): SmileIDResult.Success<T>

    /**
     * Executes the job submission process with configurable offline and skip options
     * @param skipApiSubmission If true, skips the API submission and returns a success result
     * @param offlineMode If true, performs necessary offline preparations before submission
     * @return SmileIDResult<T> containing either success data or error information
     */
    open suspend fun executeSubmission(
        skipApiSubmission: Boolean = false,
        offlineMode: Boolean = false,
    ): SmileIDResult<T> = try {
        withContext(coroutineContext) {
            when {
                skipApiSubmission -> createSuccessResult(didSubmit = false)
                else -> executeApiSubmission(offlineMode)
            }
        }
    } catch (e: Throwable) {
        SmileIDResult.Error(e)
    }

    /**
     * Executes the main API submission flow including authentication, prep upload, and final upload
     * @param offlineMode If true, performs offline preparation before submission
     * @return SmileIDResult<T> containing the submission result
     */
    protected open suspend fun executeApiSubmission(offlineMode: Boolean): SmileIDResult<T> {
        if (offlineMode) {
            handleOfflinePreparation()
        }
        return try {
            val authResponse = executeAuthentication()
            val prepUploadResponse = executePrepUpload(authResponse)
            executeUpload(authResponse, prepUploadResponse)
            createSuccessResult(didSubmit = true)
        } catch (e: Throwable) {
            SmileIDResult.Error(e)
        }
    }

    /**
     * Executes the authentication step of the submission process
     * @throws SmileIDException if authentication fails
     * @return AuthenticationResponse from the API
     */
    private suspend fun executeAuthentication(): AuthenticationResponse {
        return try {
            SmileID.api.authenticate(createAuthRequest())
        } catch (e: Exception) {
            throw when (e) {
                is HttpException -> e.toSmileIDException()
                else -> e
            }
        }
    }

    /**
     * Executes the prep upload step with the authentication response
     * @param authResponse The response from the authentication step
     * @return PrepUploadResponse from the API
     */
    private suspend fun executePrepUpload(
        authResponse: AuthenticationResponse?,
    ): PrepUploadResponse {
        val prepUploadRequest = createPrepUploadRequest(authResponse)
        return executePrepUploadWithRetry(prepUploadRequest)
    }

    /**
     * Executes the prep upload request with retry logic for specific error cases
     * @param prepUploadRequest The request to be sent
     * @param isRetry Flag indicating if this is a retry attempt
     * @throws smileIDException if prep upload fails and can't be retried
     * @return PrepUploadResponse from the API
     */
    private suspend fun executePrepUploadWithRetry(
        prepUploadRequest: PrepUploadRequest,
        isRetry: Boolean = false,
    ): PrepUploadResponse {
        return try {
            SmileID.api.prepUpload(prepUploadRequest)
        } catch (e: HttpException) {
            val smileIDException = e.toSmileIDException()
            if (!isRetry && smileIDException.details.code == ERROR_CODE_RETRY) {
                executePrepUploadWithRetry(prepUploadRequest.copy(retry = true), true)
            } else {
                throw smileIDException
            }
        }
    }

    /**
     * Executes the final upload step of the submission process
     * @param authResponse The response from the authentication step
     * @param prepUploadResponse The response from the prep upload step
     * @throws SmileIDException if upload fails
     */
    private suspend fun executeUpload(
        authResponse: AuthenticationResponse?,
        prepUploadResponse: PrepUploadResponse,
    ) {
        try {
            val uploadRequest = createUploadRequest(authResponse)
            SmileID.api.upload(prepUploadResponse.uploadUrl, uploadRequest)
        } catch (e: Exception) {
            throw when (e) {
                is HttpException -> e.toSmileIDException()
                else -> e
            }
        }
    }

    /**
     * Override this method to implement offline preparation logic
     * Called when offlineMode is true before executing the submission
     */
    open suspend fun handleOfflinePreparation() {
        val authRequest = createAuthRequest()
        createAuthenticationRequestFile(jobId, authRequest)
        createPrepUploadFile(
            jobId,
            createPrepUploadRequest(),
        )
    }

    companion object {
        // Error code for retrying the prep upload request
        private const val ERROR_CODE_RETRY = "2215"
    }
}
