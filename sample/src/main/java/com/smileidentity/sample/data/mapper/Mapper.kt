package com.smileidentity.sample.data.mapper

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
import com.smileidentity.sample.data.database.model.Job
import java.util.Date

fun EnhancedKycResponse.toJob() = Job(
    jobType = EnhancedKyc,
    // Enhanced KYC is a synchronous response
    timestamp = Date().toString(),
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

// TODO: Make this support offline mode
fun JobStatusResponse.toJob(userId: String, jobId: String, jobType: JobType) = Job(
    jobType = jobType,
    timestamp = timestamp,
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
