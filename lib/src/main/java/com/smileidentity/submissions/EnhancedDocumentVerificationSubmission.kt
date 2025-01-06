package com.smileidentity.submissions

import com.smileidentity.models.JobType
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.results.EnhancedDocumentVerificationResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.submissions.base.BaseDocumentVerificationSubmission
import java.io.File

class EnhancedDocumentVerificationSubmission(
    jobId: String,
    userId: String,
    countryCode: String,
    documentType: String?,
    allowNewEnroll: Boolean,
    documentFrontFile: File,
    selfieFile: File,
    documentBackFile: File? = null,
    livenessFiles: List<File>? = null,
    extraPartnerParams: Map<String, String>,
    metadata: List<Metadatum>? = null,
) : BaseDocumentVerificationSubmission<EnhancedDocumentVerificationResult>(
    jobId = jobId,
    userId = userId,
    jobType = JobType.EnhancedDocumentVerification,
    countryCode = countryCode,
    documentType = documentType,
    allowNewEnroll = allowNewEnroll,
    documentFrontFile = documentFrontFile,
    selfieFile = selfieFile,
    documentBackFile = documentBackFile,
    livenessFiles = livenessFiles,
    extraPartnerParams = extraPartnerParams,
    metadata = metadata,
) {
    override suspend fun createSuccessResult(didSubmit: Boolean) = SmileIDResult.Success(
        createResultInstance(
            selfieFile,
            documentFrontFile,
            livenessFiles,
            documentBackFile,
            didSubmit,
        ),
    )

    override fun createResultInstance(
        selfieFile: File,
        documentFrontFile: File,
        livenessFiles: List<File>?,
        documentBackFile: File?,
        didSubmitJob: Boolean,
    ) = EnhancedDocumentVerificationResult(
        selfieFile = selfieFile,
        documentFrontFile = documentFrontFile,
        livenessFiles = livenessFiles,
        documentBackFile = documentBackFile,
        didSubmitEnhancedDocVJob = didSubmitJob,
    )
}
