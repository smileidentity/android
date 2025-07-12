package com.smileidentity.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.JobType
import com.smileidentity.sample.data.database.model.Job
import com.smileidentity.sample.data.repository.JobsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JobsUI(
    val jobType: JobType,
    val timestamp: String,
    val userId: String,
    val jobId: String,
    val jobComplete: Boolean = false,
    val jobSuccess: Boolean = false,
    val code: String? = null,
    val resultCode: String? = null,
    val smileJobId: String? = null,
    val resultText: String? = null,
    val selfieImageUrl: String? = null,
    val livenessImagesUrl: List<String>? = emptyList(),
    val documentFrontImageUrl: String? = null,
    val documentBackImageUrl: String? = null,
)

sealed interface JobUIState {
    object Loading : JobUIState
    object Empty : JobUIState
    data class Error(val message: String) : JobUIState
    data class Success(val jobs: List<JobsUI?>) : JobUIState
}

@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: JobsRepository,
) : ViewModel() {

    // load all unsubmitted and submitted jobs here and populate jobs database
    init {
        getLocalJobs()
    }

    val uiState = repository.fetchJobs()
        .map { jobs ->
            JobUIState.Success(jobs = jobs.map { it?.toUIModel() })
        }
        .onStart { JobUIState.Loading }
        .catch { JobUIState.Error(message = "An unexpected error occurred") }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000L),
            initialValue = JobUIState.Empty,
        )

    fun getLocalJobs() = viewModelScope.launch {
        getUnsubmittedJobs()
        getSubmittedJobs()
    }

    private suspend fun getUnsubmittedJobs() {
        val jobs = SmileID.getSubmittedJobs()
        repository.createJob(jobIds = jobs)
    }

    private suspend fun getSubmittedJobs() {
        val jobs = SmileID.getSubmittedJobs()
        repository.createJob(jobIds = jobs)
    }
}

fun Job.toUIModel() = JobsUI(
    jobType = jobType,
    timestamp = timestamp,
    userId = userId,
    jobId = jobId,
    jobComplete = jobComplete,
    jobSuccess = jobSuccess,
    code = code,
    resultCode = resultCode,
    smileJobId = smileJobId,
    resultText = resultText,
    selfieImageUrl = selfieImageUrl,
    livenessImagesUrl = livenessImagesUrl,
    documentFrontImageUrl = documentFrontImageUrl,
    documentBackImageUrl = documentBackImageUrl,
)
