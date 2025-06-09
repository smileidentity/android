package com.smileidentity.compose.document

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.ImplementationMode
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCameraState

private const val VIEWFINDER_SCALE = 1.3f

@Composable
internal fun SmileCameraPreview(camSelector: CamSelector, modifier: Modifier = Modifier) {
    val cameraState = rememberCameraState()

    CameraPreview(
        cameraState = cameraState,
        camSelector = camSelector,
        implementationMode = ImplementationMode.Compatible,
        scaleType = ScaleType.FillCenter,
        imageAnalyzer = null,
        isImageAnalysisEnabled = true,
        modifier = modifier
            .padding(12.dp)
            .scale(VIEWFINDER_SCALE),
    )
}
