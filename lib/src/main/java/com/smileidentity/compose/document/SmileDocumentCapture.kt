package com.smileidentity.compose.document

import android.Manifest
import android.os.OperationCanceledException
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.smileidentity.R
import com.smileidentity.compose.components.ForceBrightness
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.util.toast
import com.smileidentity.viewmodel.VIEWFINDER_SCALE
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.ImplementationMode
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import java.io.File

/**
 * SmileDocumentCapture
 *
 * @param documentType Document type of the document being capture
 * @param onResult Callback to be invoked when the document capture is successful
 * @param onError Callback to be invoked when the document capture has an error
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SmileDocumentCapture(
    onResult: (File) -> Unit,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

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

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize()
            .height(IntrinsicSize.Min),
    ) {
        Box {
            SmileCameraPreview()

            Box(
                contentAlignment = Alignment.BottomCenter,
            ) {
                DocumentShapedBoundingBox(
                    aspectRatio = 1.59f,
                    areEdgesDetected = false,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .consumeWindowInsets(WindowInsets.safeDrawing)
                        .fillMaxSize(),
                )
            }
        }
    }
}

@Composable
internal fun SmileCameraPreview(modifier: Modifier = Modifier) {
    val cameraState = rememberCameraState()
    val camSelector by rememberCamSelector(CamSelector.Back)

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
