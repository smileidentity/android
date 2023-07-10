package com.smileidentity.sample.viewmodel

import androidx.lifecycle.ViewModel
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.JobStatusResponse
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class JobsUiState(
    val processingState: ProcessingState = ProcessingState.InProgress,
    val errorMessage: String? = null,
    val jobs: List<Unit> = persistentListOf(),
)

class JobsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(JobsUiState())
    val uiState = _uiState.asStateFlow()

    private fun getCompletedJobs(): List<JobStatusResponse> {
        TODO()
    }

    private fun getPendingJobs(): List<JobStatusResponse> {
        TODO()
    }

    private fun getJobStatus(userId: String, jobId: String) {
        TODO()
    }

    fun onRetry() {
        TODO()
    }
}
