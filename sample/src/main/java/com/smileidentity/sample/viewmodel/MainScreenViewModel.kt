package com.smileidentity.sample.viewmodel

import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.JobResult
import com.smileidentity.models.JobStatusRequest
import com.smileidentity.models.JobType.BiometricKyc
import com.smileidentity.models.JobType.DocumentVerification
import com.smileidentity.models.JobType.SmartSelfieAuthentication
import com.smileidentity.models.JobType.SmartSelfieEnrollment
import com.smileidentity.networking.pollBiometricKycJobStatus
import com.smileidentity.networking.pollDocVJobStatus
import com.smileidentity.networking.pollSmartSelfieJobStatus
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.EnhancedKycResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.sample.BottomNavigationScreen
import com.smileidentity.sample.ProductScreen
import com.smileidentity.sample.R
import com.smileidentity.sample.jobResultMessageBuilder
import com.smileidentity.sample.model.toJob
import com.smileidentity.sample.repo.DataStoreRepository
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

/**
 * Wrapper for the message, so that LaunchedEffect's key changes even if we need to display the same
 * message again. We *can't* use data class because the generated equals would be the same for the
 * same String
 */
class SnackbarMessage(val value: String)

data class MainScreenUiState(
    val shouldShowSmileConfigBottomSheet: Boolean = false,
    @StringRes val appBarTitle: Int = R.string.app_name,
    val isProduction: Boolean = !SmileID.useSandbox,
    val snackbarMessage: SnackbarMessage? = null,
    val bottomNavSelection: BottomNavigationScreen = startScreen,
    val pendingJobCount: Int = 0,
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

    private fun createBackgroundJobsPoller() = viewModelScope.launch {
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
                    DocumentVerification -> SmileID.api.pollDocVJobStatus(request)
                    BiometricKyc -> SmileID.api.pollBiometricKycJobStatus(request)
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
                                it.copy(snackbarMessage = SnackbarMessage("Job Completed"))
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
                                it.copy(
                                    snackbarMessage = SnackbarMessage("Job Polling Timed Out"),
                                )
                            }
                        }
                    }
                }
            }
    }

    private fun createPendingJobCountPoller() = viewModelScope.launch {
        DataStoreRepository.getPendingJobs(SmileID.config.partnerId, !SmileID.useSandbox)
            .distinctUntilChanged()
            .map { it.size }
            .collect { count ->
                _uiState.update { it.copy(pendingJobCount = count) }
            }
    }

    /**
     * Cancel any polling, switch the environment, and then restart polling (the auth request
     * will automatically pick up the correct environment)
     */
    fun toggleEnvironment() {
        pendingJobCountJob.cancel()
        backgroundJobsPollingJob.cancel()

        SmileID.setEnvironment(!SmileID.useSandbox)

        pendingJobCountJob = createPendingJobCountPoller()
        backgroundJobsPollingJob = createBackgroundJobsPoller()

        _uiState.update { it.copy(isProduction = !SmileID.useSandbox) }
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

    fun showSmileConfigBottomSheet() {
        _uiState.update { it.copy(shouldShowSmileConfigBottomSheet = true) }
    }

    fun onSmartSelfieEnrollmentSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = ProductScreen.SmartSelfieEnrollment.label,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    fun onSmartSelfieEnrollmentResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<SmartSelfieResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val response = result.data.jobStatusResponse ?: run {
                val errorMessage = "SmartSelfie Enrollment jobStatusResponse is null"
                Timber.e(errorMessage)
                _uiState.update { it.copy(snackbarMessage = SnackbarMessage(errorMessage)) }
                return
            }
            val actualResult = response.result as? JobResult.Entry
            val message = jobResultMessageBuilder(
                jobName = "SmartSelfie Enrollment",
                jobComplete = response.jobComplete,
                jobSuccess = response.jobSuccess,
                code = response.code,
                resultCode = actualResult?.resultCode,
                resultText = actualResult?.resultText,
                suffix = "The User ID has been copied to your clipboard",
            )
            Timber.d("$message: $result")
            _uiState.update {
                it.copy(
                    clipboardText = AnnotatedString(userId),
                    snackbarMessage = SnackbarMessage(message),
                )
            }
            viewModelScope.launch {
                DataStoreRepository.addPendingJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = response.toJob(userId, jobId, true),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "SmartSelfie Enrollment error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage(message)) }
        }
    }

    fun onSmartSelfieAuthenticationSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = ProductScreen.SmartSelfieAuthentication.label,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    fun onSmartSelfieAuthenticationResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<SmartSelfieResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val response = result.data.jobStatusResponse ?: run {
                val errorMessage = "SmartSelfie Authentication jobStatusResponse is null"
                Timber.e(errorMessage)
                _uiState.update { it.copy(snackbarMessage = SnackbarMessage(errorMessage)) }
                return
            }
            val actualResult = response.result as? JobResult.Entry
            val message = jobResultMessageBuilder(
                jobName = "SmartSelfie Authentication",
                jobComplete = response.jobComplete,
                jobSuccess = response.jobSuccess,
                code = response.code,
                resultCode = actualResult?.resultCode,
                resultText = actualResult?.resultText,
            )
            Timber.d("$message: $result")
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage(message)) }
            viewModelScope.launch {
                DataStoreRepository.addPendingJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = response.toJob(userId, jobId, true),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "SmartSelfie Authentication error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage(message)) }
        }
    }

    fun onEnhancedKycSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = ProductScreen.EnhancedKyc.label,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    fun onEnhancedKycResult(result: SmileIDResult<EnhancedKycResult>) {
        if (result is SmileIDResult.Success) {
            val resultData = result.data.response
            val message = jobResultMessageBuilder(
                jobName = "Enhanced KYC",
                jobComplete = true,
                jobSuccess = true,
                code = null,
                resultCode = resultData.resultCode,
                resultText = resultData.resultText,
            )
            Timber.d("$message: $result")
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage(message)) }
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
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage(message)) }
        }
    }

    fun onBiometricKycSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = ProductScreen.BiometricKyc.label,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    fun onBiometricKycResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<BiometricKycResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val response = result.data.jobStatusResponse
            val actualResult = response.result as? JobResult.Entry
            Timber.d("Biometric KYC Result: $result")
            val message = jobResultMessageBuilder(
                jobName = "Biometric KYC",
                jobComplete = response.jobComplete,
                jobSuccess = response.jobSuccess,
                code = response.code,
                resultCode = actualResult?.resultCode,
                resultText = actualResult?.resultText,
            )
            Timber.d("$message: $result")
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage(message)) }
            viewModelScope.launch {
                DataStoreRepository.addPendingJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = response.toJob(userId, jobId),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "Biometric KYC error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage(message)) }
        }
    }

    fun onDocumentVerificationSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = ProductScreen.DocumentVerification.label,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    fun onDocumentVerificationResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<DocumentVerificationResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val response = result.data.jobStatusResponse
            val actualResult = response.result as? JobResult.Entry
            val message = jobResultMessageBuilder(
                jobName = "Document Verification",
                jobComplete = response.jobComplete,
                jobSuccess = response.jobSuccess,
                code = response.code,
                resultCode = actualResult?.resultCode,
                resultText = actualResult?.resultText,
            )
            Timber.d("$message: $result")
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage(message)) }
            viewModelScope.launch {
                DataStoreRepository.addPendingJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = response.toJob(userId, jobId),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "Document Verification error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage(message)) }
        }
    }

    fun clearJobs() {
        viewModelScope.launch {
            DataStoreRepository.clearJobs(SmileID.config.partnerId, !SmileID.useSandbox)
        }
    }
}
