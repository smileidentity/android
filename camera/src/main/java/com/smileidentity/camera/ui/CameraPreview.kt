package com.smileidentity.camera.ui

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.smileidentity.camera.state.CameraState
import com.smileidentity.camera.state.rememberCameraState

/**
 * Creates a Camera Preview's composable.
 */
@Composable
internal fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraState: CameraState = rememberCameraState(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraIsInitialized by rememberUpdatedState(cameraState.isInitialized)
    var cameraOffset by remember { mutableStateOf(Offset.Zero) }

    AndroidView(
        modifier = modifier.onGloballyPositioned { cameraOffset = it.positionInParent() },
        factory = { context ->
            PreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                controller = cameraState.controller.apply {
                    bindToLifecycle(lifecycleOwner)
                }
                previewStreamState.observe(lifecycleOwner) { state ->
                    cameraState.isStreaming = state == PreviewView.StreamState.STREAMING
                }
            }
        },
        update = { preview ->
            if (cameraIsInitialized) {
                with(preview) {
                }
            }
        },
    )
}
