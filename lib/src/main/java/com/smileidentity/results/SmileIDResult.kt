package com.smileidentity.results

import android.os.Parcelable
import com.smileidentity.models.EnhancedKycRequest
import com.smileidentity.models.EnhancedKycResponse
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
    /*** The flow was successful. The result is the value of type [T].
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
 * The result of a smartselfie verification which consist of
 *  @param selfieFile the captured selfie file
 *  @param livenessFiles optional selfie liveness files
 *  @param didSubmitSmartSelfieJob true if the job has been
 *  submitted to the SmileID apis and false if offline is enabled and the
 *  network request failed
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class SmartSelfieResult(
    val selfieFile: File,
    val livenessFiles: List<File>,
    val didSubmitSmartSelfieJob: Boolean,
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
 * The result of a document verification which consist of
 *  @param selfieFile the captured selfie file
 *  @param documentFrontFile the captured front of document file
 *  @param livenessFiles optional selfie liveness files
 *  @param documentBackFile optional back of document file
 *  @param didSubmitDocumentVerificationJob true if the job has been
 *  submitted to the SmileID apis and false if offline is enabled and the
 *  network request failed
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class DocumentVerificationResult(
    val selfieFile: File,
    val documentFrontFile: File,
    val livenessFiles: List<File>? = null,
    val documentBackFile: File? = null,
    val didSubmitDocumentVerificationJob: Boolean,
) : Parcelable

/**
 * The result of a enhanced document verification which consist of
 *  @param selfieFile the captured selfie file
 *  @param documentFrontFile the captured front of document file
 *  @param livenessFiles optional selfie liveness files
 *  @param documentBackFile optional back of document file
 *  @param didSubmitEnhancedDocVJob true if the job has been
 *  submitted to the SmileID apis and false if offline is enabled and the
 *  network request failed
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class EnhancedDocumentVerificationResult(
    val selfieFile: File,
    val documentFrontFile: File,
    val livenessFiles: List<File>? = null,
    val documentBackFile: File? = null,
    val didSubmitEnhancedDocVJob: Boolean,
) : Parcelable

/**
 * The result of a biometric kyc verification which consist of
 *  @param selfieFile the captured selfie file
 *  @param livenessFiles optional selfie liveness files
 *  @param didSubmitBiometricKycJob true if the job has been
 *  submitted to the SmileID apis and false if offline is enabled and the
 *  network request failed
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class BiometricKycResult(
    val selfieFile: File,
    val livenessFiles: List<File>,
    val didSubmitBiometricKycJob: Boolean,
) : Parcelable
