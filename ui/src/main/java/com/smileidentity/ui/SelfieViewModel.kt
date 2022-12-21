package com.smileidentity.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SelfieUiState(
    val currentDirective: String,
    val allowAgentMode: Boolean = true,
    val progress: Float = 0f,
)

class SelfieViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SelfieUiState("Smile for the camera!"))
    val uiState: StateFlow<SelfieUiState> = _uiState.asStateFlow()

    fun switchCamera() {

    }

    fun takePicture() {

    }
}
