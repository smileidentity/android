package com.smileidentity.ui.components

import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.smileidentity.ui.utils.ForceMaxBrightness

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun SmileIDCameraPreview(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val viewfinderZoom = 1.1f
    val isInPreview = LocalInspectionMode.current

    // Force maximum brightness in order to light up the user's face
    // Only force brightness in real runtime
    if (!isInPreview) {
        ForceMaxBrightness()
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isInPreview) {
            Box(
                modifier = Modifier
                    .testTag(tag = "smile_camera_preview")
                    .fillMaxSize()
                    .scale(scale = viewfinderZoom)
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size = 48.dp),
                )
                content()
            }
        } else {
            // replace camera implementation here
        }
    }
}
