package com.smileidentity.camera.state

import android.content.ContentResolver
import android.content.Context
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recording
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.smileidentity.camera.extensions.compatMainExecutor
import java.util.concurrent.Executor

/**
 * A state object that can be hoisted to control camera, take picture or record video.
 *
 * To be created use [com.smileidentity.camera.util.rememberCameraState].
 * */
@Stable
class CameraState(context: Context) {

    /**
     * Main Executor to action as take picture or record.
     * */
    private val mainExecutor: Executor = context.compatMainExecutor

    /**
     * Content resolver to picture and video.
     * */
    private val contentResolver: ContentResolver = context.contentResolver

    /**
     * Main controller from CameraX. useful in cases that haven't been release some feature yet.
     * */
    val controller: LifecycleCameraController = LifecycleCameraController(context)

    /**
     * Record controller to video Capture
     * */
    private var recordController: Recording? = null

    /**
     * Check if camera is streaming or not.
     * */
    var isStreaming: Boolean by mutableStateOf(false)
        internal set

    /**
     * Check if camera state is initialized or not.
     * */
    var isInitialized: Boolean by mutableStateOf(false)
        internal set

    /**
     * Get scale type from the camera.
     * */
    internal var scaleType: ScaleType = ScaleType.FillCenter

    /**
     * Get implementation mode from the camera.
     * */
    internal var implementationMode: ImplementationMode = ImplementationMode.Performance

    internal var videoQualitySelector: QualitySelector
        get() = controller.videoCaptureQualitySelector
        set(value) {
            if (controller.isRecording) return
            controller.videoCaptureQualitySelector = value
        }

    /**
     * Update all values from camera state.
     * */
    internal fun update() {
    }
}
