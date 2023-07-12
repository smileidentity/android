package com.smileidentity.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.sample.repo.DataStoreRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import timber.log.Timber

data class JobsUiState(
    val processingState: ProcessingState = ProcessingState.InProgress,
    val errorMessage: String? = null,
)

/**
 * The job list to show is determined by a composite key of partnerId and environment
 */
class JobsViewModel(isProduction: Boolean) : ViewModel() {
    private val _uiState = MutableStateFlow(JobsUiState())
    val uiState = _uiState.asStateFlow()
    val jobs = DataStoreRepository.getJobs(SmileID.config.partnerId, isProduction).catch {
        Timber.e(it)
        _uiState.update { it.copy(processingState = ProcessingState.Error) }
    }.onEach {
        _uiState.update { it.copy(processingState = ProcessingState.Success) }
    }.stateIn(
        scope = viewModelScope,
        started = Eagerly,
        initialValue = persistentListOf(),
    )
}
