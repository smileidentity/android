package com.smileidentity.results

import android.os.Parcelable
import com.smileidentity.models.BiometricKycJobStatusResponse
import com.smileidentity.models.DocumentVerificationJobStatusResponse
import com.smileidentity.models.EnhancedDocumentVerificationJobStatusResponse
import com.smileidentity.models.EnhancedKycRequest
import com.smileidentity.models.EnhancedKycResponse
import com.smileidentity.models.SmartSelfieJobStatusResponse
import com.squareup.moshi.JsonClass
import java.io.File
import kotlinx.parcelize.Parcelize

/**
 * This callback is only consumed from Kotlin code, so typealias is fine. Java code will use the
 * Fragment compatibility layer (e.g. [com.smileidentity.fragment.SmartSelfieEnrollmentFragment]),
 * in which the result is delivered via the Fragment Result API. Our Fragment compatibility layer
 * is written in Kotlin.
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
     * [com.smileidentity.models.SmartSelfieJobStatusResponse.jobComplete]. If not yet complete, the
     * job status will need to be fetched again later. If the job is complete, the final job success
     * can be checked with [com.smileidentity.models.SmartSelfieJobStatusResponse.jobSuccess].
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
 * can be checked with [SmartSelfieJobStatusResponse.jobComplete]. If not yet complete, the job
 * status will need to be fetched again later. If the job is complete, the final job success can be
 * checked with [SmartSelfieJobStatusResponse.jobSuccess].
 *
 * If [jobStatusResponse] is null, that means submission to the API was skipped
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class SmartSelfieResult(
    val selfieFile: File,
    val livenessFiles: List<File>,
    val jobStatusResponse: SmartSelfieJobStatusResponse?,
) : Parcelable

/**
 * Enhanced KYC flow and API requests were successful
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class EnhancedKycResult(
    val request: EnhancedKycRequest,
    val response: EnhancedKycResponse?,
) : Parcelable

/**
 * The result of a Document Verification capture and submission to the Smile ID API. Indicates that
 * the capture and network requests were successful. The Job itself may or may not be complete yet.
 * This can be checked with [DocumentVerificationJobStatusResponse.jobComplete]. If not yet
 * complete, the job status will need to be fetched again later. If the job is complete, the final
 * job success can be checked with [DocumentVerificationJobStatusResponse.jobSuccess].
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class DocumentVerificationResult(
    val selfieFile: File,
    val documentFrontFile: File,
    val documentBackFile: File? = null,
    val jobStatusResponse: DocumentVerificationJobStatusResponse?,
) : Parcelable

/**
 * The result of Enhanced Document Verification capture and submission to the Smile ID API.
 * Indicates that the capture and network requests were successful. The Job itself may or may not
 * be complete yet.
 * This can be checked with [EnhancedDocumentVerificationJobStatusResponse.jobComplete]. If not yet
 * complete, the job status will need to be fetched again later. If the job is complete, the final
 * job success can be checked with [EnhancedDocumentVerificationJobStatusResponse.jobSuccess].
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class EnhancedDocumentVerificationResult(
    val selfieFile: File,
    val documentFrontFile: File,
    val documentBackFile: File? = null,
    val jobStatusResponse: EnhancedDocumentVerificationJobStatusResponse?,
) : Parcelable

/**
 * The result of a Biometric KYC capture and submission to the Smile ID API. Indicates that the
 * capture and network requests were successful. The Job itself may or may not be complete yet. This
 * can be checked with [BiometricKycJobStatusResponse.jobComplete]. If not yet complete, the job
 * status will need to be fetched again later. If the job is complete, the final job success can be
 * checked with [BiometricKycJobStatusResponse.jobSuccess].
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class BiometricKycResult(
    val selfieFile: File,
    val livenessFiles: List<File>,
    val jobStatusResponse: BiometricKycJobStatusResponse?,
) : Parcelable
