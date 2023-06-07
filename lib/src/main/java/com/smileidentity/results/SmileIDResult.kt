package com.smileidentity.results

import android.os.Parcelable
import com.smileidentity.models.DocVJobStatusResponse
import com.smileidentity.models.EnhancedKycRequest
import com.smileidentity.models.EnhancedKycResponse
import com.smileidentity.models.JobStatusResponse
import kotlinx.parcelize.Parcelize
import java.io.File

/**
 * This callback is only consumed from Kotlin code, so typealias is fine. Java code will use the
 * Fragment compatibility layer (e.g. [com.smileidentity.fragment.SmartSelfieRegistrationFragment]),
 * in which the result is delivered via the Fragment Result API. Our Fragment compatibility layer
 * is written in Kotlin
 */
typealias SmileIDCallback<T> = (SmileIDResult<T>) -> Unit

/**
 * The result of an SDK flow invocation. This is a sealed class, so it can only be one of the two
 * subclasses: [SmileIDResult.Success] or [SmileIDResult.Error].
 *
 * (We don't use [kotlin.Result] because we want the result to be Parcelable)
 */
sealed interface SmileIDResult<out T : Parcelable> : Parcelable {
    /**
     * The flow was successful. The result is the value of type [T].
     *
     * NB! The Job itself may or may not be complete yet. This can be checked with
     * [com.smileidentity.models.JobStatusResponse.jobComplete]. If not yet complete, the job status
     * will need to be fetched again later. If the job is complete, the final job success can be
     * checked with [com.smileidentity.models.JobStatusResponse.jobSuccess]
     */
    @Parcelize
    data class Success<T : Parcelable>(val data: T) : SmileIDResult<T>

    /**
     * An error was encountered during the flow. This includes, but is not limited to, denied
     * permissions, file errors, network errors, API errors, and unexpected errors.
     */
    @Parcelize
    data class Error(val throwable: Throwable) : SmileIDResult<Nothing>
}

/**
 * The result of a SmartSelfie capture and submission to the Smile ID API. Indicates that the selfie
 * capture and network requests were successful. The Job itself may or may not be complete yet. This
 * can be checked with [JobStatusResponse.jobComplete]. If not yet complete, the job status will
 * need to be fetched again later. If the job is complete, the final job success can be checked with
 * [JobStatusResponse.jobSuccess]
 */
@Parcelize
data class SmartSelfieResult(
    val selfieFile: File,
    val livenessFiles: List<File>,
    val jobStatusResponse: JobStatusResponse,
) : Parcelable

/**
 * Enhanced KYC flow and API requests were successful
 */
@Parcelize
data class EnhancedKycResult(
    val request: EnhancedKycRequest,
    val response: EnhancedKycResponse,
) : Parcelable

/**
 * The result of a Document Verification capture and submission to the Smile ID API. Indicates that
 * the capture and network requests were successful. The Job itself may or may not be complete yet.
 * This can be checked with [DocVJobStatusResponse.jobComplete]. If not yet complete, the job status
 * will need to be fetched again later. If the job is complete, the final job success can be checked
 * with [DocVJobStatusResponse.jobSuccess]
 */
@Parcelize
data class DocumentVerificationResult(
    val selfieFile: File? = null,
    val documentFrontFile: File,
    val documentBackFile: File? = null,
    val jobStatusResponse: DocVJobStatusResponse,
) : Parcelable
