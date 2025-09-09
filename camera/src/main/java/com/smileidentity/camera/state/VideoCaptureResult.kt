package com.smileidentity.camera.state

import android.net.Uri
import androidx.compose.runtime.Immutable

/**
 * Video Result of recording video.
 *
 * @see CameraState.startRecording
 * @see CameraState.toggleRecording
 * */
sealed interface VideoCaptureResult {
    @Immutable
    data class Success(val savedUri: Uri?) : VideoCaptureResult

    @Immutable
    data class Error(val message: String, val throwable: Throwable?) : VideoCaptureResult
}
