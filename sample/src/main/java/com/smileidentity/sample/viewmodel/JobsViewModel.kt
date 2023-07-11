package com.smileidentity.sample.viewmodel

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.JobType
import com.smileidentity.sample.label
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DateFormat.SHORT
import java.text.DateFormat.getDateTimeInstance
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class JobsUiState(
    val processingState: ProcessingState = ProcessingState.InProgress,
    val errorMessage: String? = null,
    val jobs: ImmutableList<Job> = persistentListOf(),
)

data class Job(
    @DrawableRes val icon: Int,
    val timestamp: String,
    @StringRes val jobType: Int,
    val jobStatus: String?,
    val jobMessage: String?,
)

// The job list to show is determined by a composite key of partnerId and environment
class JobsViewModel(private val isProduction: Boolean) : ViewModel() {
    private val _uiState = MutableStateFlow(JobsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)
            _uiState.update { it.copy(processingState = ProcessingState.Error) }
            delay(1000)
            _uiState.update {
                it.copy(
                    processingState = ProcessingState.Success,
                    jobs = persistentListOf(
                        Job(
                            icon = R.drawable.si_smart_selfie_instructions_hero,
                            timestamp = toHumanReadableTimestamp("2023-07-10T21:58:07.183Z"),
                            jobType = JobType.SmartSelfieEnrollment.label,
                            jobStatus = "Rejected",
                            jobMessage = "Failed Authentication - Spoof Detected",
                        ),
                        Job(
                            icon = R.drawable.si_doc_v_instructions_hero,
                            timestamp = toHumanReadableTimestamp("2023-07-10T21:58:07.183Z"),
                            jobType = JobType.SmartSelfieEnrollment.label,
                            jobStatus = null,
                            jobMessage = null,
                        ),
                    ),
                )
            }
        }
    }

    private fun getCompletedJobs(): List<Job> {
        TODO()
    }

    private fun getPendingJobs(): List<Job> {
        TODO()
    }

    private fun getJobStatus(userId: String, jobId: String) {
        TODO()
    }

    fun onRetry() {
        TODO()
    }

    companion object {
        private val outputFormat = getDateTimeInstance(SHORT, SHORT)
        private val inputFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

        /**
         * Converts "2023-07-10T21:58:07.183Z" to "7/10/23, 2:58 PM" (assuming PST timezone)
         */
        private fun toHumanReadableTimestamp(timestamp: String): String {
            return try {
                val date = inputFormat.parse(timestamp)
                outputFormat.format(date)
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse timestamp: $timestamp")
                timestamp
            }
        }
    }
}
