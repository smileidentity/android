package com.smileidentity.camera.ui

import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smileidentity.camera.util.transformToUiCoords
import com.smileidentity.camera.viewmodel.CameraPreviewViewModel
import kotlinx.coroutines.awaitCancellation

/**
 * Creates a Camera Preview's composable.
 */
@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun CameraPreview(
    viewModel: CameraPreviewViewModel,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val sensorFaceRects by viewModel.sensorFaceRects.collectAsStateWithLifecycle()
    val transformationInfo by
        produceState<SurfaceRequest.TransformationInfo?>(null, surfaceRequest) {
            try {
                surfaceRequest?.setTransformationInfoListener(Runnable::run) { transformationInfo ->
                    value = transformationInfo
                }
                awaitCancellation()
            } finally {
                surfaceRequest?.clearTransformationInfoListener()
            }
        }
    val shouldSpotlightFaces by remember {
        derivedStateOf { sensorFaceRects.isNotEmpty() && transformationInfo != null }
    }
    val context = LocalContext.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(
            context = context.applicationContext,
            lifecycleOwner = lifecycleOwner,
        )
    }

    surfaceRequest?.let { request ->
        val coordinateTransformer = remember { MutableCoordinateTransformer() }
        CameraXViewfinder(
            modifier = modifier,
            surfaceRequest = request,
            coordinateTransformer = coordinateTransformer,
        )

        AnimatedVisibility(shouldSpotlightFaces, enter = fadeIn(), exit = fadeOut()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val uiFaceRects = sensorFaceRects.transformToUiCoords(
                    transformationInfo = transformationInfo,
                    uiToBufferCoordinateTransformer = coordinateTransformer,
                )

                // Fill the whole space with the color
                drawRect(Color(0xDDE60991))

                // Then extract each face and make it transparent
                uiFaceRects.forEach { faceRect ->
                    drawRect(
                        Brush.radialGradient(
                            0.4f to Color.Black,
                            1f to Color.Transparent,
                            center = faceRect.center,
                            radius = faceRect.minDimension * 2f,
                        ),
                        blendMode = BlendMode.DstOut,
                    )
                }
            }
        }
    }
}
