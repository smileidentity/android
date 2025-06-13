package com.smileidentity.compose.document.composable

import android.Manifest
import android.os.OperationCanceledException
import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.smileidentity.R
import com.smileidentity.compose.components.ForceBrightness
import com.smileidentity.compose.document.DocumentShapedBoundingBox
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.util.toast
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.rememberCamSelector
import java.io.File

/**
 * SmileDocumentCapture
 *
 * @param documentType Document type of the document being captured
 * @param onResult Callback to be invoked when the document capture is successful
 * @param onError Callback to be invoked when the document capture has an error
 */
@Suppress("UnsafeOptInUsage")
@OptIn(ExperimentalGetImage::class, ExperimentalPermissionsApi::class)
@Composable
fun SmileDocumentCapture(
    onResult: (File) -> Unit,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val camSelector by rememberCamSelector(CamSelector.Back)
    var areEdgesDetected by remember { mutableStateOf((false)) }

    ForceBrightness()

    BackHandler { onError(OperationCanceledException("User cancelled")) }

    val permissionState = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (!granted) {
            // We don't jump to the settings screen here (unlike in CameraPermissionButton)
            // because it would cause an infinite loop of permission requests due to the
            // LaunchedEffect requesting the permission again. We should leave this decision to the
            // caller.
            onError(OperationCanceledException("Camera permission denied"))
        }
    }

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
        if (permissionState.status.shouldShowRationale) {
            context.toast(R.string.si_camera_permission_rationale)
        }
    }

    val analyzer = IdentityDocumentAnalyzer(
        luminanceThreshold = 50,
        onResult = {
                needsMoreLighting: Boolean,
                detectedDocument: Boolean,
                isDocumentGlared: Boolean,
                isDocumentBlurry: Boolean,
                isDocumentTilted: Boolean,
            ->
        },
        onError = { throwable -> onError(throwable) },
    )

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            SmileCameraPreview(
                camSelector = camSelector,
                imageAnalyzer = { imageProxy: ImageProxy, cameraState: CameraState ->
                    analyzer.analyze(imageProxy = imageProxy)
                },
            )

            DocumentShapedBoundingBox(
                aspectRatio = 3.375f / 2.125f,
                areEdgesDetected = areEdgesDetected,
                modifier = Modifier
                    .align(Alignment.TopCenter),
            )

            // QualityOverlay(
            //     qualityState = debouncedQualityState,
            //     modifier = Modifier
            //         .align(Alignment.BottomCenter)
            //         .fillMaxWidth()
            //         .padding(16.dp),
            // )
        }
    }
}

@SmilePreviews
@Composable
private fun SmileDocumentCapturePreview() {
    Preview {
        SmileDocumentCapture(
            onResult = {},
            onError = {},
        )
    }
}
