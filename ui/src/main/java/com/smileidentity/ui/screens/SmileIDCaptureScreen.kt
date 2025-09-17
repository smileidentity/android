package com.smileidentity.ui.screens

import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.smileidentity.ml.states.IdentityScanState
import com.smileidentity.ui.components.DocumentShapedView
import com.smileidentity.ui.components.FaceShapedView
import com.smileidentity.ui.components.SmileIDButton
import com.smileidentity.ui.components.SmileIDCameraPreview
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.PreviewContent
import java.io.File

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun SmileIDCaptureScreen(
    scanType: IdentityScanState.ScanType,
    modifier: Modifier = Modifier,
    continueButton: @Composable (onResult: (File) -> Unit) -> Unit = { onResult ->
        SmileIDButton(
            text = "Continue",
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
                .testTag(tag = "capture:continue_button"),
            onClick = { onResult },
        )
    },
    onResult: (File) -> Unit,
) {
    SmileIDCameraPreview(
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

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            continueButton {}
        }
    }
}

@DevicePreviews
@Composable
private fun SmileIDCaptureScreenPreview() {
    PreviewContent {
        SmileIDCaptureScreen(scanType = IdentityScanState.ScanType.SELFIE) {}
    }
}
