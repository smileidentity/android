package com.smileidentity.viewmodel.document

import android.os.Parcelable
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.models.JobType
import com.smileidentity.models.SmileIDException
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.EnhancedDocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.submissions.DocumentVerificationSubmission
import com.smileidentity.submissions.EnhancedDocumentVerificationSubmission
import com.smileidentity.submissions.base.BaseJobSubmission
import com.smileidentity.submissions.base.BaseSubmissionViewModel
import com.smileidentity.util.FileType
import com.smileidentity.util.StringResource
import com.smileidentity.util.getFileByType
import com.smileidentity.util.getFilesByType
import com.smileidentity.util.handleOfflineJobFailure
import com.smileidentity.util.isNetworkFailure
import com.smileidentity.util.moveJobToSubmitted
import io.sentry.Breadcrumb
import io.sentry.SentryLevel
import java.io.File
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

internal data class OrchestratedDocumentUiState(
    val currentStep: DocumentCaptureFlow = DocumentCaptureFlow.FrontDocumentCapture,
    val errorMessage: StringResource = StringResource.ResId(R.string.si_processing_error_subtitle),
)

/**
 * @param selfieFile The selfie image file to use for authentication. If null, selfie capture will
 * be performed
 */
internal abstract class OrchestratedDocumentViewModel<T : Parcelable>(
    protected val userId: String,
    protected val jobType: JobType,
    protected val jobId: String,
    protected val allowNewEnroll: Boolean,
    protected val countryCode: String,
    protected val documentType: String? = null,
    private val captureBothSides: Boolean,
    private val skipApiSubmission: Boolean = false,
    protected var selfieFile: File? = null,
    protected var extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    protected val metadata: MutableList<Metadatum>,
) : BaseSubmissionViewModel<T>() {
    private val _uiState = MutableStateFlow(
        OrchestratedDocumentUiState(),
    )
    val uiState = _uiState.asStateFlow()

    // var result: SmileIDResult<T> = SmileIDResult.Error(
    //     IllegalStateException("Document Capture incomplete"),
    // )
    protected var documentFrontFile: File? = null
    private var documentBackFile: File? = null
    protected var livenessFiles: List<File>? = null
    private var stepToRetry: DocumentCaptureFlow? = null

    fun onDocumentFrontCaptureSuccess(documentImageFile: File) {
        documentFrontFile = documentImageFile
        if (captureBothSides) {
            _uiState.update { it.copy(currentStep = DocumentCaptureFlow.BackDocumentCapture) }
        } else if (selfieFile == null) {
            _uiState.update { it.copy(currentStep = DocumentCaptureFlow.SelfieCapture) }
        } else {
            submitJob()
        }
    }

    fun onDocumentBackSkip() {
        if (selfieFile == null) {
            _uiState.update { it.copy(currentStep = DocumentCaptureFlow.SelfieCapture) }
        } else {
            submitJob()
        }
    }

    fun onDocumentBackCaptureSuccess(documentImageFile: File) {
        documentBackFile = documentImageFile
        if (selfieFile == null) {
            _uiState.update { it.copy(currentStep = DocumentCaptureFlow.SelfieCapture) }
        } else {
            submitJob()
        }
    }

    fun onSelfieCaptureSuccess(it: SmileIDResult.Success<SmartSelfieResult>) {
        selfieFile = it.data.selfieFile
        livenessFiles = it.data.livenessFiles
        submitJob()
    }

    abstract fun saveResult(
        selfieImage: File,
        documentFrontFile: File,
        documentBackFile: File?,
        livenessFiles: List<File>?,
        didSubmitJob: Boolean,
    )

    private fun submitJob() {
        submitJob(jobId, skipApiSubmission, SmileID.allowOfflineMode)
    }

    private fun sendResult(
        documentFrontFile: File,
        documentBackFile: File? = null,
        livenessFiles: List<File>? = null,
    ) {
        var selfieFileResult: File = selfieFile ?: run {
            Timber.w("Selfie file not found for job ID: $jobId")
            throw Exception("Selfie file not found for job ID: $jobId")
        }
        var livenessFilesResult = livenessFiles
        var documentFrontFileResult = documentFrontFile
        var documentBackFileResult = documentBackFile
        val copySuccess = moveJobToSubmitted(jobId)
        if (copySuccess) {
            selfieFileResult = getFileByType(jobId, FileType.SELFIE) ?: run {
                Timber.w("Selfie file not found for job ID: $jobId")
                throw IllegalStateException("Selfie file not found for job ID: $jobId")
            }
            livenessFilesResult = getFilesByType(jobId, FileType.LIVENESS)
            documentFrontFileResult = getFileByType(jobId, FileType.DOCUMENT_FRONT) ?: run {
                Timber.w("Document front file not found for job ID: $jobId")
                throw IllegalStateException("Document front found for job ID: $jobId")
            }
            documentBackFileResult = getFileByType(jobId, FileType.DOCUMENT_BACK)
        } else {
            Timber.w("Failed to move job $jobId to complete")
            SmileIDCrashReporting.hub.addBreadcrumb(
                Breadcrumb().apply {
                    category = "Offline Mode"
                    message = "Failed to move job $jobId to complete"
                    level = SentryLevel.INFO
                },
            )
        }

        saveResult(
            selfieImage = selfieFileResult,
            documentFrontFile = documentFrontFileResult,
            documentBackFile = documentBackFileResult,
            livenessFiles = livenessFilesResult,
            didSubmitJob = true,
        )

        _uiState.update {
            it.copy(
                currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Success),
                errorMessage = StringResource.ResId(R.string.si_doc_v_processing_success_subtitle),
            )
        }
    }

    /**
     * Trigger the display of the Error dialog
     */
    fun onError(throwable: Throwable) {
        val didMoveToSubmitted = handleOfflineJobFailure(jobId, throwable)
        if (didMoveToSubmitted) {
            this.selfieFile = getFileByType(jobId, FileType.SELFIE)
            this.livenessFiles = getFilesByType(jobId, FileType.LIVENESS)
        }
        stepToRetry = uiState.value.currentStep
        _uiState.update {
            it.copy(
                currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Error),
                errorMessage = StringResource.ResId(R.string.si_processing_error_subtitle),
            )
        }
    }

    /**
     * If [stepToRetry] is ProcessingScreen, we're retrying a network issue, so we need to kick off
     * the resubmission manually. Otherwise, we're retrying a capture error, so we just need to
     * reset the UI state
     */
    fun onRetry() {
        // The step to retry is the one that failed, which should have been saved in onError.
        // onError sets the current step to ProcessingScreen Error.
        val step = stepToRetry
        stepToRetry = null
        step?.let { stepToRetry ->
            _uiState.update { it.copy(currentStep = stepToRetry) }
            if (stepToRetry is DocumentCaptureFlow.ProcessingScreen) {
                submitJob()
            }
        }
    }

    fun onFinished(callback: SmileIDCallback<T>) = callback(result!!)

    override fun createSubmission(): BaseJobSubmission<T> {
        throw NotImplementedError("This method should not be called")
    }

    override fun processingState() {
        _uiState.update {
            it.copy(currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.InProgress))
        }
    }

    override fun handleSuccess(data: T) {
        sendResult(
            documentFrontFile
                ?: throw IllegalStateException("Document front file is null"),
            documentBackFile,
            livenessFiles,
        )
    }

    override fun handleError(error: Throwable) {
        val errorMessage: StringResource = when {
            isNetworkFailure(error) -> StringResource.ResId(R.string.si_no_internet)
            error is SmileIDException -> StringResource.ResIdFromSmileIDException(error)
            else -> StringResource.ResId(R.string.si_processing_error_subtitle)
        }
        result = SmileIDResult.Error(error)
        _uiState.update {
            it.copy(
                currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Error),
                errorMessage = errorMessage,
            )
        }
    }

    override fun handleSubmissionFiles(jobId: String) {
    }

    override fun handleOfflineSuccess() {
        _uiState.update {
            it.copy(
                currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Success),
                errorMessage = StringResource.ResId(R.string.si_offline_message),
            )
        }
        saveResult(
            selfieImage = selfieFile
                ?: throw IllegalStateException("Selfie file is null"),
            documentFrontFile = documentFrontFile ?: throw IllegalStateException(
                "Document front file is null",
            ),
            documentBackFile = documentBackFile,
            livenessFiles = livenessFiles,
            didSubmitJob = false,
        )
    }
}

internal class DocumentVerificationViewModel(
    jobType: JobType = JobType.DocumentVerification,
    userId: String,
    jobId: String,
    allowNewEnroll: Boolean,
    countryCode: String,
    documentType: String? = null,
    captureBothSides: Boolean,
    selfieFile: File? = null,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    metadata: MutableList<Metadatum>,
) : OrchestratedDocumentViewModel<DocumentVerificationResult>(
    jobType = jobType,
    userId = userId,
    jobId = jobId,
    allowNewEnroll = allowNewEnroll,
    countryCode = countryCode,
    documentType = documentType,
    captureBothSides = captureBothSides,
    selfieFile = selfieFile,
    extraPartnerParams = extraPartnerParams,
    metadata = metadata,
) {

    override fun createSubmission(): BaseJobSubmission<DocumentVerificationResult> {
        return DocumentVerificationSubmission(
            userId = userId,
            jobId = jobId,
            countryCode = countryCode,
            allowNewEnroll = allowNewEnroll,
            documentFrontFile = documentFrontFile
                ?: throw IllegalStateException("Document front file is null"),
            livenessFiles = livenessFiles.orEmpty(),
            selfieFile = selfieFile
                ?: throw IllegalStateException("Selfie file is null"),
            extraPartnerParams = extraPartnerParams,
            metadata = metadata,
        )
    }

    override fun saveResult(
        selfieImage: File,
        documentFrontFile: File,
        documentBackFile: File?,
        livenessFiles: List<File>?,
        didSubmitJob: Boolean,
    ) {
        result = SmileIDResult.Success(
            DocumentVerificationResult(
                selfieFile = selfieImage,
                documentFrontFile = documentFrontFile,
                livenessFiles = livenessFiles,
                documentBackFile = documentBackFile,
                didSubmitDocumentVerificationJob = didSubmitJob,
            ),
        )
    }
}

internal class EnhancedDocumentVerificationViewModel(
    jobType: JobType = JobType.EnhancedDocumentVerification,
    userId: String,
    jobId: String,
    allowNewEnroll: Boolean,
    countryCode: String,
    documentType: String? = null,
    captureBothSides: Boolean,
    selfieFile: File? = null,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    metadata: MutableList<Metadatum>,
) : OrchestratedDocumentViewModel<EnhancedDocumentVerificationResult>(
    jobType = jobType,
    userId = userId,
    jobId = jobId,
    allowNewEnroll = allowNewEnroll,
    countryCode = countryCode,
    documentType = documentType,
    captureBothSides = captureBothSides,
    selfieFile = selfieFile,
    extraPartnerParams = extraPartnerParams,
    metadata = metadata,
) {

    override fun createSubmission(): BaseJobSubmission<EnhancedDocumentVerificationResult> {
        return EnhancedDocumentVerificationSubmission(
            userId = userId,
            jobId = jobId,
            allowNewEnroll = allowNewEnroll,
            documentFrontFile = documentFrontFile
                ?: throw IllegalStateException("Document front file is null"),
            livenessFiles = livenessFiles.orEmpty(),
            selfieFile = selfieFile
                ?: throw IllegalStateException("Selfie file is null"),
            countryCode = countryCode,
            documentType = documentType,
            extraPartnerParams = extraPartnerParams,
            metadata = metadata,
        )
    }

    override fun saveResult(
        selfieImage: File,
        documentFrontFile: File,
        documentBackFile: File?,
        livenessFiles: List<File>?,
        didSubmitJob: Boolean,
    ) {
        result = SmileIDResult.Success(
            EnhancedDocumentVerificationResult(
                selfieFile = selfieImage,
                documentFrontFile = documentFrontFile,
                livenessFiles = livenessFiles,
                documentBackFile = documentBackFile,
                didSubmitEnhancedDocVJob = didSubmitJob,
            ),
        )
    }
}
