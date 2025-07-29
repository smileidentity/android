package com.smileidentity.camera.ui

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.smileidentity.camera.state.CameraState
import com.smileidentity.camera.state.ImplementationMode
import com.smileidentity.camera.state.ScaleType
import com.smileidentity.camera.util.rememberCameraState

/**
 * Creates a Camera Preview's composable.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraState: CameraState = rememberCameraState(),
    scaleType: ScaleType = ScaleType.FillCenter,
    implementationMode: ImplementationMode = ImplementationMode.Performance,
    content: @Composable () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraIsInitialized by rememberUpdatedState(cameraState.isInitialized)

    AndroidView(
        modifier = modifier,
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
                    this.scaleType = scaleType.type
                    this.implementationMode = implementationMode.value
                }

                cameraState.update()
            }
        },
    )

    content()
}
