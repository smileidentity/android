package com.smileidentity.submissions

import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.JobType.SmartSelfieAuthentication
import com.smileidentity.models.JobType.SmartSelfieEnrollment
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.models.v2.SmartSelfieResponse
import com.smileidentity.models.v2.asNetworkRequest
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.networking.doSmartSelfieAuthentication
import com.smileidentity.networking.doSmartSelfieEnrollment
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.submissions.base.BaseSynchronousJobSubmission
import com.smileidentity.util.FileType
import com.smileidentity.util.getFileByType
import com.smileidentity.util.getFilesByType
import com.smileidentity.util.moveJobToSubmitted
import io.sentry.Breadcrumb
import io.sentry.SentryLevel
import java.io.File
import timber.log.Timber

class SelfieSubmission(
    private val isEnroll: Boolean,
    private val userId: String,
    jobId: String,
    private val allowNewEnroll: Boolean,
    private val selfieFile: File,
    private val livenessFiles: List<File>,
    private val extraPartnerParams: Map<String, String>,
    private val metadata: List<Metadatum>? = null,
) : BaseSynchronousJobSubmission<SmartSelfieResult, SmartSelfieResponse>(jobId) {

    override fun createAuthRequest(): AuthenticationRequest {
        return AuthenticationRequest(
            jobType = if (isEnroll) SmartSelfieEnrollment else SmartSelfieAuthentication,
            enrollment = isEnroll,
            userId = userId,
            jobId = jobId,
        )
    }

    override fun createPrepUploadRequest(authResponse: AuthenticationResponse?) = PrepUploadRequest(
        partnerParams = PartnerParams(
            jobType = if (isEnroll) SmartSelfieEnrollment else SmartSelfieAuthentication,
            jobId = jobId,
            userId = userId,
            extras = extraPartnerParams,
        ),
        allowNewEnroll = allowNewEnroll.toString(),
        metadata = metadata,
        timestamp = authResponse?.timestamp ?: "",
        signature = authResponse?.signature ?: "",
    )

    override fun createUploadRequest(authResponse: AuthenticationResponse?): UploadRequest {
        return UploadRequest(
            images = listOfNotNull(
                selfieFile.asSelfieImage(),
            ) + livenessFiles.map { it.asLivenessImage() },
        )
    }
    override suspend fun getApiResponse(): SmartSelfieResponse? {
        val apiResponse = if (isEnroll) {
            SmileID.api.doSmartSelfieEnrollment(
                selfieImage = selfieFile,
                livenessImages = livenessFiles,
                userId = userId,
                partnerParams = extraPartnerParams,
                allowNewEnroll = allowNewEnroll,
                metadata = metadata?.asNetworkRequest(),
            )
        } else {
            SmileID.api.doSmartSelfieAuthentication(
                selfieImage = selfieFile,
                livenessImages = livenessFiles,
                userId = userId,
                partnerParams = extraPartnerParams,
                metadata = metadata?.asNetworkRequest(),
            )
        }
        return apiResponse
    }

    override fun createSynchronousRes(
        result: SmartSelfieResponse?,
    ): SmileIDResult.Success<SmartSelfieResult> {
        // Move files from unsubmitted to submitted directories
        if (result != null) {
            val copySuccess = moveJobToSubmitted(jobId)
            if (copySuccess) {
                val selfieFileResult = getFileByType(jobId, FileType.SELFIE) ?: run {
                    Timber.w("Selfie file not found for job ID: $jobId")
                    throw IllegalStateException("Selfie file not found for job ID: $jobId")
                }
                val livenessFilesResult = getFilesByType(jobId, FileType.LIVENESS)
                selfieFileResult to livenessFilesResult
            } else {
                Timber.w("Failed to move job $jobId to complete")
                SmileIDCrashReporting.hub.addBreadcrumb(
                    Breadcrumb().apply {
                        category = "Offline Mode"
                        message = "Failed to move job $jobId to complete"
                        level = SentryLevel.INFO
                    },
                )
                selfieFile to livenessFiles
            }
        }
        return SmileIDResult.Success(
            SmartSelfieResult(
                selfieFile = selfieFile,
                livenessFiles = livenessFiles,
                apiResponse = result,
            ),
        )
    }
}
