package com.smileidentity.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smileidentity.ml.states.IdentityScanState
import com.smileidentity.ui.components.SmileCameraPreview
import com.smileidentity.ui.components.DocumentShapedView
import com.smileidentity.ui.components.FaceShapedView
import java.io.File

@Composable
fun SmileIDCaptureView(
    scanType: IdentityScanState.ScanType,
    modifier: Modifier = Modifier,
    onResult: (File) -> Unit,
) {
    SmileCameraPreview(
        modifier = modifier,
    ) {
        /**
         * We have different scan types here to allow us different analyzer depending on what we are
         * scanning
         *
         * document has back and front so we can check for images on front side, and not on back side
         * for example :)
         */
        when (scanType) {
            IdentityScanState.ScanType.DOCUMENT_FRONT -> DocumentShapedView()
            IdentityScanState.ScanType.DOCUMENT_BACK -> DocumentShapedView()
            IdentityScanState.ScanType.SELFIE -> FaceShapedView()
        }
    }
}

@ThemePreviews
@DevicePreviews
@Composable
private fun SmileIDCaptureViewPreview() {
    SmileIDCaptureView(scanType = IdentityScanState.ScanType.SELFIE) {}
}
