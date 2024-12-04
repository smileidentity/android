package com.smileidentity.submissions

import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.submissions.base.BaseJobSubmission
import java.io.File

class BiometricKYCSubmission(
    private val userId: String,
    jobId: String,
    private val allowNewEnroll: Boolean,
    private val livenessFiles: List<File>,
    private val selfieFile: File,
    private val idInfo: IdInfo,
    private val extraPartnerParams: Map<String, String>,
    private val metadata: List<Metadatum>? = null,
) : BaseJobSubmission<BiometricKycResult>(jobId) {

    override fun createAuthRequest() = AuthenticationRequest(
        jobType = JobType.BiometricKyc,
        enrollment = false,
        userId = userId,
        jobId = jobId,
        country = idInfo.country,
        idType = idInfo.idType,
    )

    override fun createPrepUploadRequest(authResponse: AuthenticationResponse?) = PrepUploadRequest(
        partnerParams = authResponse?.partnerParams?.copy(extras = extraPartnerParams)
            ?: PartnerParams(
                jobType = JobType.BiometricKyc,
                jobId = jobId,
                userId = userId,
                extras = extraPartnerParams,
            ),
        // TODO : Michael will change this to boolean
        allowNewEnroll = allowNewEnroll.toString(),
        metadata = metadata,
        signature = authResponse?.signature ?: "",
        timestamp = authResponse?.timestamp ?: "",
    )

    override fun createUploadRequest(authResponse: AuthenticationResponse?): UploadRequest {
        val selfieImageInfo = selfieFile.asSelfieImage()
        val livenessImageInfo = livenessFiles.map { it.asLivenessImage() }
        val uploadRequest = UploadRequest(
            images = listOfNotNull(
                selfieImageInfo,
            ) + livenessImageInfo,
            idInfo = idInfo.copy(entered = true),
        )
        return uploadRequest
    }

    override suspend fun createSuccessResult(
        didSubmit: Boolean,
    ): SmileIDResult.Success<BiometricKycResult> {
        return SmileIDResult.Success(
            BiometricKycResult(
                selfieFile = selfieFile,
                livenessFiles = livenessFiles,
                didSubmitBiometricKycJob = didSubmit,
            ),
        )
    }
}
