package com.smileidentity.sample.model

import com.smileidentity.models.BiometricKycJobStatusResponse
import com.smileidentity.models.DocumentVerificationJobStatusResponse
import com.smileidentity.models.EnhancedDocumentVerificationJobStatusResponse
import com.smileidentity.models.EnhancedKycResponse
import com.smileidentity.models.JobResult
import com.smileidentity.models.JobStatusResponse
import com.smileidentity.models.JobType
import com.smileidentity.models.JobType.BiometricKyc
import com.smileidentity.models.JobType.DocumentVerification
import com.smileidentity.models.JobType.EnhancedDocumentVerification
import com.smileidentity.models.JobType.EnhancedKyc
import com.smileidentity.models.JobType.SmartSelfieAuthentication
import com.smileidentity.models.JobType.SmartSelfieEnrollment
import com.smileidentity.models.SmartSelfieJobStatusResponse
import com.smileidentity.sample.repo.DataStoreRepository
import com.squareup.moshi.JsonClass
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * This class is used by the [DataStoreRepository] to store Preferences/to be shown on the Jobs
 * screen. It is saved to Preferences by saving as JSON String using Moshi. As a result, be very
 * careful about breaking changes to the JSON schema!
 */
@JsonClass(generateAdapter = true)
data class Job(
    val jobType: JobType,
    val timestamp: String,
    val userId: String,
    val jobId: String,
    val jobComplete: Boolean,
    val jobSuccess: Boolean,
    val code: String?,
    val resultCode: String?,
    val smileJobId: String?,
    val resultText: String?,
    val selfieImageUrl: String?,
)

private val outputFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
private val inputFormat =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

/**
 * Converts "2023-07-10T21:58:07.183Z" to "7/10/23, 2:58 PM" (assuming PST timezone)
 */
private fun toHumanReadableTimestamp(timestamp: String): String {
    return try {
        val date = inputFormat.parse(timestamp) as Date
        outputFormat.format(date)
    } catch (e: Exception) {
        Timber.e(e, "Failed to parse timestamp: $timestamp")
        timestamp
    }
}

fun EnhancedKycResponse.toJob() = Job(
    jobType = EnhancedKyc,
    // Enhanced KYC is a synchronous response
    timestamp = toHumanReadableTimestamp(inputFormat.format(Date())),
    userId = partnerParams.userId,
    jobId = partnerParams.jobId,
    jobComplete = true,
    jobSuccess = true,
    code = null,
    resultCode = resultCode,
    smileJobId = smileJobId,
    resultText = resultText,
    // Enhanced KYC *does* return a Base 64 encoded selfie image, but we are not using it here
    // due to performance concerns w.r.t. disk I/O
    selfieImageUrl = null,
)

fun SmartSelfieJobStatusResponse.toJob(userId: String, jobId: String, isEnrollment: Boolean) =
    toJob(
        userId = userId,
        jobId = jobId,
        jobType = if (isEnrollment) SmartSelfieEnrollment else SmartSelfieAuthentication,
    )

fun BiometricKycJobStatusResponse.toJob(userId: String, jobId: String) = toJob(
    userId = userId,
    jobId = jobId,
    jobType = BiometricKyc,
)

fun DocumentVerificationJobStatusResponse.toJob(userId: String, jobId: String) = toJob(
    userId = userId,
    jobId = jobId,
    jobType = DocumentVerification,
)

fun EnhancedDocumentVerificationJobStatusResponse.toJob(userId: String, jobId: String) = toJob(
    userId = userId,
    jobId = jobId,
    jobType = EnhancedDocumentVerification,
)

fun JobStatusResponse.toJob(
    userId: String,
    jobId: String,
    jobType: JobType,
) = Job(
    jobType = jobType,
    timestamp = toHumanReadableTimestamp(timestamp),
    userId = userId,
    jobId = jobId,
    jobComplete = jobComplete,
    jobSuccess = jobSuccess,
    code = code,
    resultCode = (result as? JobResult.Entry)?.resultCode,
    smileJobId = (result as? JobResult.Entry)?.smileJobId,
    resultText = (result as? JobResult.Entry)?.resultText,
    selfieImageUrl = imageLinks?.selfieImageUrl,
)
