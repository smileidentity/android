package com.smileidentity.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.smileidentity.ml.states.IdentityScanState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

abstract class IdentityScanViewModel : ViewModel() {

    private val _scannerState: MutableStateFlow<State> =
        MutableStateFlow(value = State.Initializing)

    internal val scannerState: StateFlow<State> = _scannerState

    internal sealed class State {
        data object Initializing : State()
        class Scanning(val scanState: IdentityScanState? = null) : State()
        class Scanned(val result: IdentityScanState) : State()
        class Timeout : State()
    }

    fun startScan(scanType: IdentityScanState.ScanType) {
        _scannerState.update { State.Scanning() }
    }

    fun resetScannerState() {
        _scannerState.update { State.Initializing }
    }
}
