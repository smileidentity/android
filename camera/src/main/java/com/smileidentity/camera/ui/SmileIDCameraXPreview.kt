package com.smileidentity.camera.ui

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.video.QualitySelector
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.smileidentity.camera.observeAsState
import com.smileidentity.camera.state.CamSelector
import com.smileidentity.camera.state.CameraState
import com.smileidentity.camera.state.CaptureMode
import com.smileidentity.camera.state.ImageAnalyzer
import com.smileidentity.camera.state.ImageCaptureMode
import com.smileidentity.camera.state.ImageTargetSize
import com.smileidentity.camera.state.ImplementationMode
import com.smileidentity.camera.state.ScaleType
import com.smileidentity.camera.util.rememberCameraState

@Composable
fun SmileIDCameraXPreview(
    modifier: Modifier = Modifier,
    cameraState: CameraState = rememberCameraState(),
    camSelector: CamSelector = cameraState.camSelector,
    captureMode: CaptureMode = cameraState.captureMode,
    imageCaptureMode: ImageCaptureMode = cameraState.imageCaptureMode,
    imageCaptureTargetSize: ImageTargetSize? = cameraState.imageCaptureTargetSize,
    scaleType: ScaleType = cameraState.scaleType,
    exposureCompensation: Int = cameraState.initialExposure,
    zoomRatio: Float = 1F,
    imageAnalyzer: ImageAnalyzer? = null,
    implementationMode: ImplementationMode = cameraState.implementationMode,
    isImageAnalysisEnabled: Boolean = cameraState.isImageAnalysisEnabled,
    videoQualitySelector: QualitySelector = cameraState.videoQualitySelector,
    onPreviewStreamChanged: () -> Unit = {},
    onSwitchToFront: @Composable (Bitmap) -> Unit = {},
    onSwitchToBack: @Composable (Bitmap) -> Unit = {},
    onZoomRatioChanged: (Float) -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleEvent by lifecycleOwner.lifecycle.observeAsState()
    val cameraIsInitialized by rememberUpdatedState(cameraState.isInitialized)
    val isCameraIdle by rememberUpdatedState(!cameraState.isStreaming)
    var latestBitmap by remember { mutableStateOf<Bitmap?>(null) }

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
        update = { previewView ->
            if (cameraIsInitialized) {
                with(previewView) {
                    this.scaleType = scaleType.type
                    this.implementationMode = implementationMode.value
                    latestBitmap = when {
                        lifecycleEvent == Lifecycle.Event.ON_STOP -> null
                        !isCameraIdle && camSelector != cameraState.camSelector -> bitmap
                        else -> latestBitmap
                    }
                    cameraState.update(
                        camSelector = camSelector,
                        captureMode = captureMode,
                        imageCaptureTargetSize = imageCaptureTargetSize,
                        scaleType = scaleType,
                        isImageAnalysisEnabled = isImageAnalysisEnabled,
                        imageAnalyzer = imageAnalyzer,
                        implementationMode = implementationMode,
                        zoomRatio = zoomRatio,
                        imageCaptureMode = imageCaptureMode,
                        videoQualitySelector = videoQualitySelector,
                        exposureCompensation = exposureCompensation,
                    )
                }
            }
        },
    )

    if (isCameraIdle) {
        latestBitmap?.let {
            when (camSelector.selector) {
                CameraSelector.DEFAULT_FRONT_CAMERA -> onSwitchToFront(it)
                CameraSelector.DEFAULT_BACK_CAMERA -> onSwitchToBack(it)
                else -> Unit
            }
            LaunchedEffect(latestBitmap) {
                onPreviewStreamChanged()
                if (latestBitmap != null) onZoomRatioChanged(cameraState.minZoom)
            }
        }
    }

    content()
}
