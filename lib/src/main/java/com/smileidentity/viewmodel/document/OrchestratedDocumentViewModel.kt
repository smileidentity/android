package com.smileidentity.viewmodel.document

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.updateOrAddBy
import com.smileidentity.metadata.updaters.DeviceOrientationMetadata
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.ConsentInformation
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.SmileIDException
import com.smileidentity.models.UploadRequest
import com.smileidentity.networking.asDocumentBackImage
import com.smileidentity.networking.asDocumentFrontImage
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.EnhancedDocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.FileType
import com.smileidentity.util.StringResource
import com.smileidentity.util.createAuthenticationRequestFile
import com.smileidentity.util.createPrepUploadFile
import com.smileidentity.util.createUploadRequestFile
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.getFileByType
import com.smileidentity.util.getFilesByType
import com.smileidentity.util.handleOfflineJobFailure
import com.smileidentity.util.isNetworkFailure
import com.smileidentity.util.moveJobToSubmitted
import com.smileidentity.util.toSmileIDException
import io.sentry.Breadcrumb
import io.sentry.SentryLevel
import java.io.File
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
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
    private val jobType: JobType,
    private val userId: String,
    protected val jobId: String,
    private val allowNewEnroll: Boolean,
    private val countryCode: String,
    private val documentType: String? = null,
    private val consentInformation: ConsentInformation? = null,
    private val useStrictMode: Boolean = false,
    private val captureBothSides: Boolean,
    protected var selfieFile: File? = null,
    private var extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    private val metadata: MutableList<Metadatum>,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrchestratedDocumentUiState())
    val uiState = _uiState.asStateFlow()
    var result: SmileIDResult<T> = SmileIDResult.Error(
        IllegalStateException("Document Capture incomplete"),
    )
    private var documentFrontFile: File? = null
    private var documentBackFile: File? = null
    private var livenessFiles: List<File>? = null
    private var stepToRetry: DocumentCaptureFlow? = null
    private var networkRetries = 0

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
        val documentFrontFile = documentFrontFile
            ?: throw IllegalStateException("documentFrontFile is null")
        _uiState.update {
            it.copy(currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.InProgress))
        }

        try {
            DeviceOrientationMetadata.shared.storeDeviceMovement()
        } catch (_: UninitializedPropertyAccessException) {
            /*
            In case .shared isn't initialised it throws the above exception. Given that the
            device movement is only metadata we ignore it and take no action.
             */
        }

        viewModelScope.launch(getExceptionHandler(::onError)) {
            val authRequest = AuthenticationRequest(
                jobType = jobType,
                enrollment = false,
                userId = userId,
                jobId = jobId,
            )
            val frontImageInfo = documentFrontFile.asDocumentFrontImage()
            val backImageInfo = documentBackFile?.asDocumentBackImage()
            val selfieImageInfo = selfieFile?.asSelfieImage() ?: throw IllegalStateException(
                "Selfie file is null",
            )
            // Liveness files will be null when the partner bypasses our Selfie capture with a file
            val livenessImageInfo = livenessFiles.orEmpty().map { it.asLivenessImage() }
            val uploadRequest = UploadRequest(
                images = listOfNotNull(
                    frontImageInfo,
                    backImageInfo,
                    selfieImageInfo,
                ) +
                    livenessImageInfo,
                idInfo = IdInfo(countryCode, documentType),
                consentInformation = consentInformation,
            )

            if (SmileID.allowOfflineMode) {
                createAuthenticationRequestFile(jobId, authRequest)
                createPrepUploadFile(
                    jobId = jobId,
                    prepUploadRequest = PrepUploadRequest(
                        partnerParams = PartnerParams(
                            jobType = jobType,
                            jobId = jobId,
                            userId = userId,
                            extras = extraPartnerParams,
                        ),
                        allowNewEnroll = allowNewEnroll,
                        metadata = metadata,
                        timestamp = "",
                        signature = "",
                    ),
                )
                createUploadRequestFile(
                    jobId = jobId,
                    uploadRequest = uploadRequest,
                )
            }

            val authResponse = SmileID.api.authenticate(authRequest)

            metadata.add(Metadatum.NetworkRetries(networkRetries))

            val prepUploadRequest = PrepUploadRequest(
                partnerParams = authResponse.partnerParams.copy(extras = extraPartnerParams),
                allowNewEnroll = allowNewEnroll,
                metadata = metadata,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )

            val prepUploadResponse = runCatching {
                SmileID.api.prepUpload(prepUploadRequest)
            }.recoverCatching { throwable ->
                when {
                    throwable is HttpException -> {
                        val smileIDException = throwable.toSmileIDException()
                        if (smileIDException.details.code == "2215") {
                            networkRetries++
                            metadata.updateOrAddBy(Metadatum.NetworkRetries(networkRetries)) {
                                it.name == "network_retries"
                            }
                            SmileID.api.prepUpload(
                                prepUploadRequest.copy(
                                    retry = true,
                                    metadata = metadata,
                                ),
                            )
                        } else {
                            throw smileIDException
                        }
                    }

                    else -> {
                        throw throwable
                    }
                }
            }.getOrThrow()

            SmileID.api.upload(prepUploadResponse.uploadUrl, uploadRequest)
            Timber.d("Upload finished")
            sendResult(documentFrontFile, documentBackFile, livenessFiles)
        }
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
        val copySuccess: Boolean
        if (useStrictMode) {
            copySuccess = moveJobToSubmitted(jobId) && moveJobToSubmitted(userId)
        } else {
            copySuccess = moveJobToSubmitted(jobId)
        }
        if (copySuccess) {
            // strict mode uses userId as the file location, otherwise uses jobId
            val fileLocation = if (useStrictMode) userId else jobId
            selfieFileResult = getFileByType(fileLocation, FileType.SELFIE) ?: run {
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
            SmileIDCrashReporting.scopes.addBreadcrumb(
                Breadcrumb().apply {
                    category = "Offline Mode"
                    message = "Failed to move job $jobId to complete"
                    level = SentryLevel.INFO
                },
            )
        }

        networkRetries = 0
        metadata.removeAll { it is Metadatum.NetworkRetries }

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
        val didMoveToSubmitted: Boolean
        if (useStrictMode) {
            didMoveToSubmitted = handleOfflineJobFailure(jobId, throwable) &&
                handleOfflineJobFailure(userId, throwable)
        } else {
            didMoveToSubmitted = handleOfflineJobFailure(jobId, throwable)
        }
        if (didMoveToSubmitted) {
            this.selfieFile = getFileByType(jobId, FileType.SELFIE)
            this.livenessFiles = getFilesByType(jobId, FileType.LIVENESS)
            this.documentFrontFile = getFileByType(jobId, FileType.DOCUMENT_FRONT)
            this.documentBackFile = getFileByType(jobId, FileType.DOCUMENT_BACK)
        }
        stepToRetry = uiState.value.currentStep
        _uiState.update {
            it.copy(
                currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Error),
                errorMessage = StringResource.ResId(R.string.si_processing_error_subtitle),
            )
        }
        if (SmileID.allowOfflineMode && isNetworkFailure(throwable)) {
            _uiState.update {
                it.copy(
                    currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Success),
                    errorMessage = StringResource.ResId(R.string.si_offline_message),
                )
            }
            saveResult(
                selfieImage = selfieFile ?: throw IllegalStateException("Selfie file is null"),
                documentFrontFile = documentFrontFile ?: throw IllegalStateException(
                    "Document front file is null",
                ),
                documentBackFile = documentBackFile,
                livenessFiles = livenessFiles,
                didSubmitJob = false,
            )
        } else {
            val errorMessage: StringResource = when {
                isNetworkFailure(throwable) -> StringResource.ResId(R.string.si_no_internet)
                throwable is SmileIDException -> StringResource.ResIdFromSmileIDException(throwable)
                else -> StringResource.ResId(R.string.si_processing_error_subtitle)
            }
            result = SmileIDResult.Error(throwable)
            _uiState.update {
                it.copy(
                    currentStep = DocumentCaptureFlow.ProcessingScreen(ProcessingState.Error),
                    errorMessage = errorMessage,
                )
            }
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
                networkRetries++
                submitJob()
            }
        }
    }

    fun onFinished(callback: SmileIDCallback<T>) = callback(result)
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
    useStrictMode: Boolean = false,
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
    useStrictMode = useStrictMode,
    selfieFile = selfieFile,
    extraPartnerParams = extraPartnerParams,
    metadata = metadata,
) {

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
    consentInformation: ConsentInformation?,
    captureBothSides: Boolean,
    selfieFile: File? = null,
    useStrictMode: Boolean = false,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    metadata: MutableList<Metadatum>,
) : OrchestratedDocumentViewModel<EnhancedDocumentVerificationResult>(
    jobType = jobType,
    userId = userId,
    jobId = jobId,
    allowNewEnroll = allowNewEnroll,
    countryCode = countryCode,
    documentType = documentType,
    consentInformation = consentInformation,
    captureBothSides = captureBothSides,
    useStrictMode = useStrictMode,
    selfieFile = selfieFile,
    extraPartnerParams = extraPartnerParams,
    metadata = metadata,
) {

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
