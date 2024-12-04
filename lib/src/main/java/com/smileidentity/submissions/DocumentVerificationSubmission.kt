package com.smileidentity.submissions

import com.smileidentity.models.JobType
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.submissions.base.BaseDocumentVerificationSubmission
import java.io.File

class DocumentVerificationSubmission(
    jobId: String,
    userId: String,
    countryCode: String,
    allowNewEnroll: Boolean,
    documentFrontFile: File,
    selfieFile: File,
    documentType: String? = null,
    documentBackFile: File? = null,
    livenessFiles: List<File>? = null,
    extraPartnerParams: Map<String, String>,
    metadata: List<Metadatum>? = null,
) : BaseDocumentVerificationSubmission<DocumentVerificationResult>(
    jobId = jobId,
    userId = userId,
    jobType = JobType.DocumentVerification,
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
    ) = DocumentVerificationResult(
        selfieFile = selfieFile,
        documentFrontFile = documentFrontFile,
        livenessFiles = livenessFiles,
        documentBackFile = documentBackFile,
        didSubmitDocumentVerificationJob = didSubmitJob,
    )
}
