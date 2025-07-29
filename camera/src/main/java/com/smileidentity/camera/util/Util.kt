package com.smileidentity.camera.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.smileidentity.camera.state.CameraState

/**
 * Camera State from [CameraPreview] Composable.
 * */
@Composable
fun rememberCameraState(): CameraState {
    val context = LocalContext.current
    return remember { CameraState(context) }
}
