package com.smileidentity.submissions.base

import android.os.Parcelable
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.networking.asDocumentBackImage
import com.smileidentity.networking.asDocumentFrontImage
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import java.io.File

abstract class BaseDocumentVerificationSubmission<T : Parcelable>(
    jobId: String,
    protected val userId: String,
    protected val jobType: JobType,
    protected val countryCode: String,
    protected val documentType: String?,
    protected val allowNewEnroll: Boolean,
    protected val documentFrontFile: File,
    protected val selfieFile: File,
    protected val documentBackFile: File? = null,
    protected val livenessFiles: List<File>? = null,
    protected val extraPartnerParams: Map<String, String>,
    protected val metadata: List<Metadatum>? = null,
) : BaseJobSubmission<T>(jobId) {

    override fun createAuthRequest() = AuthenticationRequest(
        jobType = jobType,
        enrollment = false,
        userId = userId,
        jobId = jobId,
        country = countryCode,
        idType = documentType,
    )

    override fun createPrepUploadRequest(authResponse: AuthenticationResponse?) = PrepUploadRequest(
        partnerParams = authResponse?.partnerParams?.copy(extras = extraPartnerParams)
            ?: PartnerParams(
                jobType = jobType,
                jobId = jobId,
                userId = userId,
                extras = extraPartnerParams,
            ),
        allowNewEnroll = allowNewEnroll.toString(),
        metadata = metadata,
        signature = authResponse?.signature ?: "",
        timestamp = authResponse?.timestamp ?: "",
    )

    override fun createUploadRequest(authResponse: AuthenticationResponse?): UploadRequest {
        val frontImageInfo = documentFrontFile.asDocumentFrontImage()
        val backImageInfo = documentBackFile?.asDocumentBackImage()
        val selfieImageInfo = selfieFile.asSelfieImage()
        val livenessImageInfo = livenessFiles.orEmpty().map { it.asLivenessImage() }
        return UploadRequest(
            images = listOfNotNull(
                frontImageInfo,
                backImageInfo,
                selfieImageInfo,
            ) + livenessImageInfo,
            idInfo = IdInfo(countryCode, documentType),
        )
    }

    protected abstract fun createResultInstance(
        selfieFile: File,
        documentFrontFile: File,
        livenessFiles: List<File>?,
        documentBackFile: File?,
        didSubmitJob: Boolean,
    ): T
}
