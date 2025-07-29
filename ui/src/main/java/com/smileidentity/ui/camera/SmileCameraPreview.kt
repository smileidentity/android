package com.smileidentity.ui.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import com.smileidentity.camera.ui.CameraPreview
import com.smileidentity.ui.utils.ForceMaxBrightness

@Composable
fun SmileCameraPreview(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val viewfinderZoom = 1.1f

    // Force maximum brightness in order to light up the user's face
    ForceMaxBrightness()

    Box(modifier = modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier
                .testTag("smile_camera_preview")
                .fillMaxSize()
                .clipToBounds()
                // Scales the *preview* WITHOUT changing the zoom ratio, to allow capture of
                // "out of bounds" content as a fraud prevention technique
                .scale(viewfinderZoom),
        ) {
            content()
        }
    }
}
