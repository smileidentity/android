package com.smileidentity.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.smileidentity.ml.states.IdentityScanState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

abstract class IdentityScanViewModel<CameraOutput> : ViewModel() {

    private val imageChannel = Channel<CameraOutput>(capacity = Channel.RENDEZVOUS)

    fun sendImageToStream(image: CameraOutput) = try {
        imageChannel.trySend(image).onClosed {
            Timber.w("Attempted to send image to closed channel $it")
        }.onFailure {
            if (it != null) {
                Timber.w("Failure when sending image to channel $it")
            } else {
                Timber.v("No analyzers available to process image")
            }
        }.onSuccess {
            Timber.v("Successfully sent image to be processed")
        }
    } catch (e: ClosedSendChannelException) {
        Timber.w("Attempted to send image to closed channel $e")
    } catch (t: Throwable) {
        Timber.e("Unable to send image to channel $t")
    }

    /**
     * Get the stream of images from the camera. This is a hot [Flow] of images with a back pressure strategy DROP.
     * Images that are not read from the flow are dropped. This flow is backed by a [Channel].
     */
    val getImageStream: Flow<CameraOutput> = imageChannel.receiveAsFlow()

    private val _scannerState: MutableStateFlow<State> =
        MutableStateFlow(value = State.Initializing)

    internal val scannerState: StateFlow<State> = _scannerState

    internal sealed class State {
        data object Initializing : State()
        class Scanning(val scanState: IdentityScanState? = null) : State()
        class Scanned(val result: IdentityScanState) : State()
        data object Timeout : State()
    }

    fun startScan(scanType: IdentityScanState.ScanType) {
        _scannerState.update { State.Scanning() }
    }

    fun resetScannerState() {
        _scannerState.update { State.Initializing }
    }
}
