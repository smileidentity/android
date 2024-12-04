package com.smileidentity.submissions.utils

import com.smileidentity.SmileID.cleanup
import com.smileidentity.SmileID.moshi
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.results.SmileIDResult
import com.smileidentity.submissions.BiometricKYCSubmission
import com.smileidentity.submissions.DocumentVerificationSubmission
import com.smileidentity.submissions.EnhancedDocumentVerificationSubmission
import com.smileidentity.submissions.SelfieSubmission
import com.smileidentity.util.UPLOAD_REQUEST_FILE
import com.smileidentity.util.getSmileTempFile
import com.smileidentity.util.randomUserId
import java.io.File
import timber.log.Timber

internal suspend fun submitSmartSelfieJob(
    selfieFile: File,
    livenessFiles: List<File>,
    jobId: String,
    authRequest: AuthenticationRequest,
    prepUploadRequest: PrepUploadRequest,
    deleteFilesOnSuccess: Boolean,
) {
    val submission = SelfieSubmission(
        isEnroll = authRequest.jobType == JobType.SmartSelfieEnrollment,
        userId = authRequest.userId ?: randomUserId(),
        jobId = jobId,
        allowNewEnroll = prepUploadRequest.allowNewEnroll.toBoolean(),
        metadata = prepUploadRequest.metadata,
        selfieFile = selfieFile,
        livenessFiles = livenessFiles,
        extraPartnerParams = prepUploadRequest.partnerParams.extras,
    )
    when (val result = submission.executeSubmission()) {
        is SmileIDResult.Success -> {
            if (deleteFilesOnSuccess) {
                cleanup(jobId)
            }
            Timber.d("Upload finished successfully")
        }

        is SmileIDResult.Error -> {
            Timber.e(result.throwable, "Upload failed")
            throw result.throwable
        }
    }
}

internal fun getIdInfo(jobId: String): IdInfo? {
    return run {
        val uploadRequestJson = getSmileTempFile(
            jobId,
            UPLOAD_REQUEST_FILE,
            true,
        ).useLines { it.joinToString("\n") }
        val savedUploadRequestJson = moshi.adapter(UploadRequest::class.java)
            .fromJson(uploadRequestJson)
            ?: run {
                Timber.v(
                    "Error decoding UploadRequest JSON to class: " +
                        uploadRequestJson,
                )
                throw IllegalArgumentException("Invalid jobId information")
            }
        savedUploadRequestJson.idInfo
    }
}

internal suspend fun submitBiometricKYCJob(
    selfieFile: File,
    livenessFiles: List<File>,
    jobId: String,
    idInfo: IdInfo,
    authRequest: AuthenticationRequest,
    prepUploadRequest: PrepUploadRequest,
    deleteFilesOnSuccess: Boolean,
) {
    val submission = BiometricKYCSubmission(
        userId = authRequest.userId ?: randomUserId(),
        jobId = jobId,
        allowNewEnroll = prepUploadRequest.allowNewEnroll.toBoolean(),
        selfieFile = selfieFile,
        livenessFiles = livenessFiles,
        idInfo = idInfo,
        extraPartnerParams = prepUploadRequest.partnerParams.extras,
    )
    when (val result = submission.executeSubmission()) {
        is SmileIDResult.Success -> {
            if (deleteFilesOnSuccess) {
                cleanup(jobId)
            }
            Timber.d("Upload finished successfully")
        }

        is SmileIDResult.Error -> {
            Timber.e(result.throwable, "Upload failed")
            throw result.throwable
        }
    }
}

internal suspend fun submitDocumentVerificationJob(
    selfieFile: File,
    livenessFiles: List<File>,
    documentFrontFile: File,
    jobId: String,
    countryCode: String,
    authRequest: AuthenticationRequest,
    prepUploadRequest: PrepUploadRequest,
    deleteFilesOnSuccess: Boolean,
) {
    val submission = DocumentVerificationSubmission(
        userId = authRequest.userId ?: randomUserId(),
        jobId = jobId,
        allowNewEnroll = prepUploadRequest.allowNewEnroll.toBoolean(),
        documentFrontFile = documentFrontFile,
        livenessFiles = livenessFiles,
        selfieFile = selfieFile,
        countryCode = countryCode,
        extraPartnerParams = prepUploadRequest.partnerParams.extras,
        metadata = prepUploadRequest.metadata,
    )
    when (val result = submission.executeSubmission()) {
        is SmileIDResult.Success -> {
            if (deleteFilesOnSuccess) {
                cleanup(jobId)
            }
            Timber.d("Upload finished successfully")
        }

        is SmileIDResult.Error -> {
            Timber.e(result.throwable, "Upload failed")
            throw result.throwable
        }
    }
}

internal suspend fun submitEnhancedDocumentVerification(
    selfieFile: File,
    livenessFiles: List<File>,
    documentFrontFile: File,
    jobId: String,
    idInfo: IdInfo,
    authRequest: AuthenticationRequest,
    prepUploadRequest: PrepUploadRequest,
    deleteFilesOnSuccess: Boolean,
) {
    val submission = EnhancedDocumentVerificationSubmission(
        userId = authRequest.userId ?: randomUserId(),
        jobId = jobId,
        allowNewEnroll = prepUploadRequest.allowNewEnroll.toBoolean(),
        countryCode = idInfo.country,
        documentType = idInfo.idType,
        documentFrontFile = documentFrontFile,
        livenessFiles = livenessFiles,
        selfieFile = selfieFile,
        extraPartnerParams = prepUploadRequest.partnerParams.extras,
        metadata = prepUploadRequest.metadata,
    )
    when (val result = submission.executeSubmission()) {
        is SmileIDResult.Success -> {
            if (deleteFilesOnSuccess) {
                cleanup(jobId)
            }
            Timber.d("Upload finished successfully")
        }

        is SmileIDResult.Error -> {
            Timber.e(result.throwable, "Upload failed")
            throw result.throwable
        }
    }
}
