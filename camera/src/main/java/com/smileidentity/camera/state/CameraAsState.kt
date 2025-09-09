package com.smileidentity.camera.state

import androidx.camera.core.ImageAnalysis
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext

/**
 * Camera State from [CameraPreview] Composable.
 * */
@Composable
fun rememberCameraState(): CameraState {
    val context = LocalContext.current
    return remember { CameraState(context) }
}

/**
 * Camera selector's State to [CameraPreview] Composable.
 * */
@Composable
fun rememberCamSelector(selector: CamSelector = CamSelector.Back): MutableState<CamSelector> =
    rememberSaveable(saver = CamSelector.Saver) {
        mutableStateOf(selector)
    }

/**
 * Create instance remember of ImageAnalyzer.
 *
 * @see ImageAnalyzer
 * */
@Composable
fun CameraState.rememberImageAnalyzer(
    analyze: ImageAnalysis.Analyzer,
    imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy =
        ImageAnalysisBackpressureStrategy.KeepOnlyLatest,
    imageAnalysisTargetSize: ImageTargetSize? =
        ImageTargetSize(this.imageAnalysisTargetSize),
    imageAnalysisImageQueueDepth: Int = this.imageAnalysisImageQueueDepth,
): ImageAnalyzer = remember(this) {
    ImageAnalyzer(
        cameraState = this,
        imageAnalysisBackpressureStrategy = imageAnalysisBackpressureStrategy,
        imageAnalysisTargetSize = imageAnalysisTargetSize,
        imageAnalysisImageQueueDepth = imageAnalysisImageQueueDepth,
        analyzer = analyze,
    )
}
