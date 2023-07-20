package com.smileidentity.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class BvnConsentUiState(
    val bvn: String = "",
    val showWrongBvn: Boolean = false,
)

internal class BvnConsentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BvnConsentUiState())
    val uiState = _uiState.asStateFlow()



}