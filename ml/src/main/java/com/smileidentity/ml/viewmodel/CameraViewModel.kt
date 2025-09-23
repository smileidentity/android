package com.smileidentity.ml.viewmodel

import android.graphics.Rect
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber

data class CameraPreviewImage<ImageType>(val image: ImageType, val viewBounds: Rect)

abstract class CameraViewModel<CameraOutput> : ViewModel() {

    private val imageChannel = Channel<CameraOutput>(capacity = Channel.RENDEZVOUS)

    protected fun sendImageToStream(image: CameraOutput) = try {
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
}
