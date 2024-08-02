package com.smileidentity.sample.viewmodel

import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.JobStatusRequest
import com.smileidentity.models.JobType.BiometricKyc
import com.smileidentity.models.JobType.DocumentVerification
import com.smileidentity.models.JobType.EnhancedDocumentVerification
import com.smileidentity.models.JobType.SmartSelfieAuthentication
import com.smileidentity.models.JobType.SmartSelfieEnrollment
import com.smileidentity.models.v2.SmartSelfieStatus
import com.smileidentity.networking.pollBiometricKycJobStatus
import com.smileidentity.networking.pollDocumentVerificationJobStatus
import com.smileidentity.networking.pollEnhancedDocumentVerificationJobStatus
import com.smileidentity.networking.pollSmartSelfieJobStatus
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.EnhancedDocumentVerificationResult
import com.smileidentity.results.EnhancedKycResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.sample.BottomNavigationScreen
import com.smileidentity.sample.ProductScreen
import com.smileidentity.sample.R
import com.smileidentity.sample.jobResultMessageBuilder
import com.smileidentity.sample.model.Job
import com.smileidentity.sample.model.getCurrentTimeAsHumanReadableTimestamp
import com.smileidentity.sample.model.toJob
import com.smileidentity.sample.repo.DataStoreRepository
import com.smileidentity.util.getExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class MainScreenUiState(
    @StringRes val appBarTitle: Int = R.string.app_name,
    val isProduction: Boolean = !SmileID.useSandbox,
    val snackbarMessage: String? = null,
    val bottomNavSelection: BottomNavigationScreen = startScreen,
    val pendingJobCount: Int = 0,
    val showSmileConfigBottomSheet: Boolean = false,
    val clipboardText: AnnotatedString? = null,
) {
    @StringRes
    val environmentName = if (isProduction) R.string.production else R.string.sandbox

    companion object {
        val startScreen = BottomNavigationScreen.Home
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState = _uiState.asStateFlow()

    private var pendingJobCountJob = createPendingJobCountPoller()
    private var backgroundJobsPollingJob = createBackgroundJobsPoller()

    private fun createBackgroundJobsPoller() = viewModelScope.launch(
        getExceptionHandler { throwable ->
            Timber.e(throwable, "Background job polling failed")
            _uiState.update {
                it.copy(snackbarMessage = "Background job polling failed: ${throwable.message}")
            }
        },
    ) {
        val authRequest = AuthenticationRequest(SmartSelfieEnrollment)
        val authResponse = SmileID.api.authenticate(authRequest)
        DataStoreRepository.getPendingJobs(SmileID.config.partnerId, !SmileID.useSandbox)
            .distinctUntilChanged()
            .flatMapMerge { it.asFlow() }
            .collect { job ->
                val userId = job.userId
                val jobId = job.jobId
                val jobType = job.jobType
                val request = JobStatusRequest(
                    userId = userId,
                    jobId = jobId,
                    includeImageLinks = false,
                    includeHistory = false,
                    partnerId = SmileID.config.partnerId,
                    timestamp = authResponse.timestamp,
                    signature = authResponse.signature,
                )
                val pollFlow = when (jobType) {
                    SmartSelfieAuthentication -> SmileID.api.pollSmartSelfieJobStatus(request)
                    SmartSelfieEnrollment -> SmileID.api.pollSmartSelfieJobStatus(request)
                    DocumentVerification -> SmileID.api.pollDocumentVerificationJobStatus(request)
                    BiometricKyc -> SmileID.api.pollBiometricKycJobStatus(request)
                    EnhancedDocumentVerification ->
                        SmileID.api.pollEnhancedDocumentVerificationJobStatus(request)

                    else -> {
                        Timber.e("Unexpected pending job: $job")
                        throw IllegalStateException("Unexpected pending job: $job")
                    }
                }
                    .map { it.toJob(userId, jobId, jobType) }
                    .catch {
                        Timber.e(it, "Job polling failed")
                        DataStoreRepository.markPendingJobAsCompleted(
                            partnerId = SmileID.config.partnerId,
                            isProduction = !SmileID.useSandbox,
                            completedJob = job.copy(
                                jobComplete = true,
                                resultText = "Job polling error",
                            ),
                        )
                    }

                // launch, instead of immediately collecting, so that we don't block the flow
                launch {
                    // We only care about the last value - either the job completed or timed out
                    // NB! We will *not* update the state once the job has locally timed out
                    pollFlow.lastOrNull()?.let {
                        if (it.jobComplete) {
                            DataStoreRepository.markPendingJobAsCompleted(
                                partnerId = SmileID.config.partnerId,
                                isProduction = !SmileID.useSandbox,
                                completedJob = it,
                            )
                            _uiState.update {
                                it.copy(snackbarMessage = "Job Completed")
                            }
                        } else {
                            DataStoreRepository.markPendingJobAsCompleted(
                                partnerId = SmileID.config.partnerId,
                                isProduction = !SmileID.useSandbox,
                                completedJob = it.copy(
                                    jobComplete = true,
                                    resultText = "Job polling timed out",
                                ),
                            )
                            _uiState.update {
                                it.copy(snackbarMessage = "Job Polling Timed Out")
                            }
                        }
                    }
                }
            }
    }

    private fun createPendingJobCountPoller() = viewModelScope.launch(
        getExceptionHandler { throwable ->
            Timber.e(throwable, "Pending job count poller failed")
            _uiState.update {
                it.copy(snackbarMessage = "Pending job count poller failed: ${throwable.message}")
            }
        },
    ) {
        DataStoreRepository.getPendingJobs(SmileID.config.partnerId, !SmileID.useSandbox)
            .distinctUntilChanged()
            .map { it.size }
            .collect { count ->
                _uiState.update { it.copy(pendingJobCount = count) }
            }
    }

    fun onHomeSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = R.string.app_name,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    fun onJobsSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = BottomNavigationScreen.Jobs.label,
                bottomNavSelection = BottomNavigationScreen.Jobs,
            )
        }
    }

    fun onResourcesSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = BottomNavigationScreen.Resources.label,
                bottomNavSelection = BottomNavigationScreen.Resources,
            )
        }
    }

    fun onSettingsSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = BottomNavigationScreen.Settings.label,
                bottomNavSelection = BottomNavigationScreen.Settings,
            )
        }
    }

    fun onSmartSelfieEnrollmentSelected() {
        _uiState.update { it.copy(appBarTitle = ProductScreen.SmartSelfieEnrollment.label) }
    }

    fun onSmartSelfieEnrollmentResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<SmartSelfieResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val response = result.data.apiResponse ?: run {
                val errorMessage = "SmartSelfie Enrollment completed in offline mode"
                Timber.w(errorMessage)
                _uiState.update { it.copy(snackbarMessage = errorMessage) }
                return
            }
            val message = jobResultMessageBuilder(
                jobName = "SmartSelfie Enrollment",
                jobComplete = true,
                jobSuccess = true,
                code = response.code,
                resultCode = null,
                resultText = response.message,
            )
            _uiState.update {
                it.copy(clipboardText = AnnotatedString(userId), snackbarMessage = message)
            }
            viewModelScope.launch {
                DataStoreRepository.addCompletedJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = Job(
                        jobType = SmartSelfieEnrollment,
                        timestamp = response.createdAt,
                        userId = userId,
                        jobId = jobId,
                        jobComplete = true,
                        jobSuccess = response.status == SmartSelfieStatus.Approved,
                        code = response.code,
                        resultText = response.message,
                    ),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "SmartSelfie Enrollment error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun onSmartSelfieAuthenticationSelected() {
        _uiState.update { it.copy(appBarTitle = ProductScreen.SmartSelfieAuthentication.label) }
    }

    fun onSmartSelfieAuthenticationResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<SmartSelfieResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val response = result.data.apiResponse ?: run {
                val errorMessage = "SmartSelfie Authentication completed in offline mode"
                Timber.w(errorMessage)
                _uiState.update { it.copy(snackbarMessage = errorMessage) }
                return
            }
            val message = jobResultMessageBuilder(
                jobName = "SmartSelfie Authentication",
                jobComplete = true,
                jobSuccess = true,
                code = response.code,
                resultCode = null,
                resultText = response.message,
            )
            _uiState.update { it.copy(snackbarMessage = message) }
            viewModelScope.launch {
                DataStoreRepository.addCompletedJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = Job(
                        jobType = SmartSelfieAuthentication,
                        timestamp = response.createdAt,
                        userId = userId,
                        jobId = jobId,
                        jobComplete = response.status != SmartSelfieStatus.Pending,
                        jobSuccess = response.status == SmartSelfieStatus.Approved,
                        code = response.code,
                        resultText = response.message,
                    ),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "SmartSelfie Authentication error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun onSmartSelfieEnrollmentV2Selected() {
        _uiState.update { it.copy(appBarTitle = ProductScreen.SmartSelfieEnrollment.label) }
    }

    fun onSmartSelfieEnrollmentV2Result(result: SmileIDResult<SmartSelfieResult>) {
        onHomeSelected()
        if (result is SmileIDResult.Success) {
            val response = result.data.apiResponse ?: run {
                val errorMessage = "SmartSelfie Enrollment completed in offline mode"
                Timber.w(errorMessage)
                _uiState.update { it.copy(snackbarMessage = errorMessage) }
                return
            }
            val message = jobResultMessageBuilder(
                jobName = "SmartSelfie Enrollment",
                didSubmitJob = true,
                jobComplete = true,
                jobSuccess = true,
                code = response.code,
                resultCode = null,
                resultText = response.message,
            )
            _uiState.update {
                it.copy(clipboardText = AnnotatedString(response.userId), snackbarMessage = message)
            }
            viewModelScope.launch {
                DataStoreRepository.addCompletedJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = Job(
                        jobType = SmartSelfieEnrollment,
                        timestamp = response.createdAt,
                        userId = response.userId,
                        jobId = response.jobId,
                        jobComplete = true,
                        jobSuccess = true,
                        code = response.code,
                        resultText = response.message,
                    ),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "SmartSelfie Enrollment error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun onSmartSelfieAuthenticationV2Selected() {
        _uiState.update { it.copy(appBarTitle = ProductScreen.SmartSelfieAuthentication.label) }
    }

    fun onSmartSelfieAuthenticationV2Result(result: SmileIDResult<SmartSelfieResult>) {
        onHomeSelected()
        if (result is SmileIDResult.Success) {
            val response = result.data.apiResponse ?: run {
                val errorMessage = "SmartSelfie Authentication completed in offline mode"
                Timber.w(errorMessage)
                _uiState.update { it.copy(snackbarMessage = errorMessage) }
                return
            }
            val message = jobResultMessageBuilder(
                jobName = "SmartSelfie Authentication",
                didSubmitJob = true,
                jobComplete = true,
                jobSuccess = true,
                code = response.code,
                resultCode = null,
                resultText = response.message,
            )
            _uiState.update { it.copy(snackbarMessage = message) }
            viewModelScope.launch {
                DataStoreRepository.addCompletedJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = Job(
                        jobType = SmartSelfieAuthentication,
                        timestamp = response.createdAt,
                        userId = response.userId,
                        jobId = response.jobId,
                        jobComplete = true,
                        jobSuccess = true,
                        code = response.code,
                        resultText = response.message,
                    ),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "SmartSelfie Authentication error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun onEnhancedKycSelected() {
        _uiState.update { it.copy(appBarTitle = ProductScreen.EnhancedKyc.label) }
    }

    fun onEnhancedKycResult(result: SmileIDResult<EnhancedKycResult>) {
        if (result is SmileIDResult.Success) {
            val resultData = result.data.response ?: run {
                val errorMessage = "Enhanced KYC jobStatusResponse is null"
                Timber.e(errorMessage)
                _uiState.update { it.copy(snackbarMessage = errorMessage) }
                return
            }
            val message = jobResultMessageBuilder(
                jobName = "Enhanced KYC",
                jobComplete = true,
                jobSuccess = true,
                code = null,
                resultCode = resultData.resultCode,
                resultText = resultData.resultText,
            )
            Timber.d("$message: $result")
            _uiState.update { it.copy(snackbarMessage = message) }
            viewModelScope.launch {
                // Enhanced KYC completes synchronously
                DataStoreRepository.addCompletedJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = resultData.toJob(),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "Enhanced KYC error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun onBiometricKycSelected() {
        _uiState.update { it.copy(appBarTitle = ProductScreen.BiometricKyc.label) }
    }

    fun onBiometricKycResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<BiometricKycResult>,
    ) {
        if (result is SmileIDResult.Success) {
            Timber.d("Biometric KYC Result: $result")
            val message = jobResultMessageBuilder(
                jobName = "Biometric KYC",
                didSubmitJob = result.data.didSubmitBiometricKycJob,
            )
            Timber.d("$message: $jobId $userId $result")
            _uiState.update { it.copy(snackbarMessage = message) }
            if (result.data.didSubmitBiometricKycJob) {
                viewModelScope.launch {
                    DataStoreRepository.addPendingJob(
                        partnerId = SmileID.config.partnerId,
                        isProduction = uiState.value.isProduction,
                        job = Job(
                            jobType = BiometricKyc,
                            timestamp = getCurrentTimeAsHumanReadableTimestamp(),
                            userId = userId,
                            jobId = jobId,
                        ),
                    )
                }
            } else {
                Timber.w(" $jobId not saved to pending job, offline enabled")
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "Biometric KYC error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun onDocumentVerificationSelected() {
        _uiState.update { it.copy(appBarTitle = ProductScreen.DocumentVerification.label) }
    }

    fun onDocumentVerificationResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<DocumentVerificationResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val message = jobResultMessageBuilder(
                jobName = "Document Verification",
                didSubmitJob = result.data.didSubmitDocumentVerificationJob,
            )
            Timber.d("$message: $jobId $userId $result")
            _uiState.update { it.copy(snackbarMessage = message) }
            if (result.data.didSubmitDocumentVerificationJob) {
                viewModelScope.launch {
                    DataStoreRepository.addPendingJob(
                        partnerId = SmileID.config.partnerId,
                        isProduction = uiState.value.isProduction,
                        job = Job(
                            jobType = DocumentVerification,
                            timestamp = getCurrentTimeAsHumanReadableTimestamp(),
                            userId = userId,
                            jobId = jobId,
                        ),
                    )
                }
            } else {
                Timber.w(" $jobId not saved to pending job, offline enabled")
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "Document Verification error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun onBvnConsentSelected() {
        _uiState.update { it.copy(appBarTitle = ProductScreen.BvnConsent.label) }
    }

    fun onConsentDenied() {
        _uiState.update { it.copy(snackbarMessage = "Consent Denied") }
    }

    fun onSuccessfulBvnConsent() {
        _uiState.update { it.copy(snackbarMessage = "BVN Consent Successful") }
    }

    fun onEnhancedDocumentVerificationSelected() {
        _uiState.update { it.copy(appBarTitle = ProductScreen.EnhancedDocumentVerification.label) }
    }

    fun onEnhancedDocumentVerificationResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<EnhancedDocumentVerificationResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val message = jobResultMessageBuilder(
                jobName = "Enhanced Document Verification",
                didSubmitJob = result.data.didSubmitEnhancedDocVJob,
            )
            Timber.d("$message: $jobId $userId $result")
            _uiState.update { it.copy(snackbarMessage = message) }
            if (result.data.didSubmitEnhancedDocVJob) {
                viewModelScope.launch {
                    DataStoreRepository.addPendingJob(
                        partnerId = SmileID.config.partnerId,
                        isProduction = uiState.value.isProduction,
                        job = Job(
                            jobType = EnhancedDocumentVerification,
                            timestamp = getCurrentTimeAsHumanReadableTimestamp(),
                            userId = userId,
                            jobId = jobId,
                        ),
                    )
                }
            } else {
                Timber.w(" $jobId not saved to pending job, offline enabled")
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "Enhanced Document Verification error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun clearJobs() {
        viewModelScope.launch {
            DataStoreRepository.clearJobs(SmileID.config.partnerId, !SmileID.useSandbox)
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
