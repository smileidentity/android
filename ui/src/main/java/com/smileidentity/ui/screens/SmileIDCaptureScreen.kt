package com.smileidentity.ui.screens

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.camera.CameraPreviewImage
import com.smileidentity.camera.state.CamSelector
import com.smileidentity.camera.state.ImageAnalysisBackpressureStrategy
import com.smileidentity.camera.state.rememberCamSelector
import com.smileidentity.camera.state.rememberCameraState
import com.smileidentity.camera.state.rememberImageAnalyzer
import com.smileidentity.camera.util.rotate
import com.smileidentity.ml.states.IdentityScanState
import com.smileidentity.ui.components.DocumentShapedView
import com.smileidentity.ui.components.FaceShapedView
import com.smileidentity.ui.components.SmileIDCameraPreview
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.PreviewContent
import com.smileidentity.ui.utils.viewModelFactory
import com.smileidentity.ui.viewmodel.SelfieScanViewModel
import java.io.File

@SuppressLint("ComposeViewModelInjection")
@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun SmileIDCaptureScreen(
    scanType: IdentityScanState.ScanType,
    modifier: Modifier = Modifier,
    onResult: (File) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraState = rememberCameraState()
    var camSelector by rememberCamSelector(CamSelector.Front)
    val viewModel: SelfieScanViewModel = viewModel(
        factory = viewModelFactory {
            SelfieScanViewModel()
        },
    )

    SmileIDCameraPreview(
        modifier = modifier,
        imageAnalyzer = cameraState.rememberImageAnalyzer(
            analyze = { imageProxy ->
                val image = imageProxy.toBitmap()
                    .rotate(rotationDegrees = imageProxy.imageInfo.rotationDegrees.toFloat())
                viewModel.sendImageToStream(image = CameraPreviewImage(image = image))
                imageProxy.close()
            },
            imageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.KeepOnlyLatest,
        ),
    ) {
        viewModel.startScan(
            context = context,
            lifecycleOwner = lifecycleOwner,
        )

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
        ) {}
    }
}

@DevicePreviews
@Composable
private fun SmileIDCaptureScreenPreview() {
    PreviewContent {
        SmileIDCaptureScreen(scanType = IdentityScanState.ScanType.SELFIE) {}
    }
}
