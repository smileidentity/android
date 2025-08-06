package com.smileidentity.camera.util

import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.setFrom
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

internal fun List<Rect>.transformToUiCoords(
    transformationInfo: SurfaceRequest.TransformationInfo?,
    uiToBufferCoordinateTransformer: MutableCoordinateTransformer,
): List<Rect> = this.map { sensorRect ->
    val bufferToUiTransformMatrix = Matrix().apply {
        setFrom(uiToBufferCoordinateTransformer.transformMatrix)
        invert()
    }

    val sensorToBufferTransformMatrix = Matrix().apply {
        transformationInfo?.let {
            setFrom(it.sensorToBufferTransform)
        }
    }

    val bufferRect = sensorToBufferTransformMatrix.map(sensorRect)
    val uiRect = bufferToUiTransformMatrix.map(bufferRect)

    uiRect
}
