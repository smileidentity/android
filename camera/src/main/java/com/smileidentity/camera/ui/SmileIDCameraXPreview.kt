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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.smileidentity.camera.extensions.clamped
import com.smileidentity.camera.extensions.onCameraTouchEvent
import com.smileidentity.camera.observeAsState
import com.smileidentity.camera.state.CamSelector
import com.smileidentity.camera.state.CameraState
import com.smileidentity.camera.state.CaptureMode
import com.smileidentity.camera.state.FlashMode
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
    flashMode: FlashMode = cameraState.flashMode,
    scaleType: ScaleType = cameraState.scaleType,
    enableTorch: Boolean = cameraState.enableTorch,
    exposureCompensation: Int = cameraState.initialExposure,
    zoomRatio: Float = 1F,
    imageAnalyzer: ImageAnalyzer? = null,
    implementationMode: ImplementationMode = cameraState.implementationMode,
    isImageAnalysisEnabled: Boolean = cameraState.isImageAnalysisEnabled,
    isFocusOnTapEnabled: Boolean = cameraState.isFocusOnTapEnabled,
    isPinchToZoomEnabled: Boolean = cameraState.isZoomSupported,
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
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    val isCameraIdle by rememberUpdatedState(!cameraState.isStreaming)
    var latestBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cameraOffset by remember { mutableStateOf(Offset.Zero) }

    AndroidView(
        modifier = modifier.onGloballyPositioned { cameraOffset = it.positionInParent() },
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
                    onCameraTouchEvent(
                        onTap = { if (isFocusOnTapEnabled) tapOffset = it + cameraOffset },
                        onScaleChanged = {
                            if (isPinchToZoomEnabled) {
                                val zoom = zoomRatio.clamped(it).coerceIn(
                                    minimumValue = cameraState.minZoom,
                                    maximumValue = cameraState.maxZoom,
                                )
                                onZoomRatioChanged(zoom)
                            }
                        },
                    )
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
                        isFocusOnTapEnabled = isFocusOnTapEnabled,
                        flashMode = flashMode,
                        enableTorch = enableTorch,
                        zoomRatio = zoomRatio,
                        imageCaptureMode = imageCaptureMode,
                        meteringPoint = meteringPointFactory.createPoint(x, y),
                        videoQualitySelector = videoQualitySelector,
                        exposureCompensation = exposureCompensation,
                    )
                }
            }
        },
    )

    if (isCameraIdle) {
        latestBitmap?.let {
            when (camSelector.selector.lensFacing) {
                CameraSelector.LENS_FACING_FRONT -> onSwitchToFront(it)
                CameraSelector.LENS_FACING_BACK -> onSwitchToBack(it)
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
