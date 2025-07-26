package com.smileidentity.camera.state

import android.content.Context
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * A state object that can be hoisted to control camera, take picture or record video.
 *
 * To be created use [com.smileidentity.camera.util.rememberCameraState].
 * */
@Stable
class CameraState(context: Context) {

    /**
     * Main controller from CameraX. useful in cases that haven't been release some feature yet.
     * */
    val controller: LifecycleCameraController = LifecycleCameraController(context)

    /**
     * Check if camera state is initialized or not.
     * */
    var isInitialized: Boolean by mutableStateOf(false)
        internal set

    /**
     * Check if camera is streaming or not.
     * */
    var isStreaming: Boolean by mutableStateOf(false)
        internal set

    /**
     * Update all values from camera state.
     * */
    internal fun update() {
    }
}
