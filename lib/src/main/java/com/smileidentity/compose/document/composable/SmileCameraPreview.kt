package com.smileidentity.compose.document.composable

import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageAnalysisBackpressureStrategy.KeepOnlyLatest
import com.ujizin.camposer.state.ImplementationMode
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCameraState
import com.ujizin.camposer.state.rememberImageAnalyzer

private const val VIEWFINDER_SCALE = 1.3f

@Composable
internal fun SmileCameraPreview(
    camSelector: CamSelector,
    imageAnalyzer: (ImageProxy, CameraState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraState = rememberCameraState()

    CameraPreview(
        cameraState = cameraState,
        camSelector = camSelector,
        implementationMode = ImplementationMode.Compatible,
        scaleType = ScaleType.FillCenter,
        imageAnalyzer = cameraState.rememberImageAnalyzer(
            analyze = { imageProxy -> imageAnalyzer(imageProxy, cameraState) },
            // Guarantees only one image will be delivered for analysis at a time
            imageAnalysisBackpressureStrategy = KeepOnlyLatest,
        ),
        isImageAnalysisEnabled = true,
        modifier = modifier
            .padding(12.dp)
            .scale(VIEWFINDER_SCALE),
    )
}
