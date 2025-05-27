package com.smileidentity.unico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class UnicoProcessViewModel(
    private val processManager: UnicoProcessManager,
) : ViewModel() {

    private val _state = MutableStateFlow(UnicoProcessState())
    val state: StateFlow<UnicoProcessState> = _state.asStateFlow()

    fun createProcess(
        callbackUri: String,
        flow: String,
        duiType: String,
        duiValue: String,
        friendlyName: String,
        purpose: String,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val response = processManager.createProcess(
                    callbackUri = callbackUri,
                    flow = flow,
                    duiType = duiType,
                    duiValue = duiValue,
                    friendlyName = friendlyName,
                    purpose = purpose,
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    webLink = response.webLink,
                    processId = response.processId,
                )
            } catch (e: Exception) {
                Timber.d("Juuma here $e")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message,
                )
            }
        }
    }

    fun updateProgress(progress: Int) {
        _state.value = _state.value.copy(progress = progress)
    }

    fun setProcessComplete() {
        _state.value = _state.value.copy(isProcessComplete = true)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
