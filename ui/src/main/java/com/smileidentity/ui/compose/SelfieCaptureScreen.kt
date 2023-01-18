package com.smileidentity.ui.compose

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.smileidentity.ui.R
import com.smileidentity.ui.core.SelfieCaptureResultCallback
import com.smileidentity.ui.core.toast
import com.smileidentity.ui.viewmodel.SelfieViewModel
import com.smileidentity.ui.viewmodel.isCapturing
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.ImageAnalysisBackpressureStrategy.KeepOnlyLatest
import com.ujizin.camposer.state.ImplementationMode
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import com.ujizin.camposer.state.rememberImageAnalyzer

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SelfieCaptureOrPermissionScreen(
    agentMode: Boolean = false,
    manualCaptureMode: Boolean = false,
    onResult: SelfieCaptureResultCallback = SelfieCaptureResultCallback {},
) {
    SelfieCaptureOrPermissionScreen(
        agentMode,
        manualCaptureMode,
        rememberPermissionState(Manifest.permission.CAMERA),
        onResult,
    )
}

/**
 * The internal modifier is used to prevent the function from being called from outside the module
 * as this is for testing purposes only. This is because others consumers would need to use the
 * @OptIn(ExperimentalPermissionsApi::class) annotation to use this function.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun SelfieCaptureOrPermissionScreen(
    agentMode: Boolean = false,
    manualCaptureMode: Boolean = false,
    cameraPermissionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA),
    onResult: SelfieCaptureResultCallback = SelfieCaptureResultCallback {},
) {
    val context = LocalContext.current
    if (cameraPermissionState.status.isGranted) {
        SelfieCaptureScreenContent(agentMode, manualCaptureMode, onResult = onResult)
    } else {
        SideEffect {
            if (cameraPermissionState.status.shouldShowRationale) {
                cameraPermissionState.launchPermissionRequest()
            } else {
                // The user has permanently denied the permission, so we can't request it again.
                // We can, however, direct the user to the app settings screen to manually
                // enable the permission.
                context.toast(R.string.si_camera_permission_rationale)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    }
}

@Preview
@Composable
fun SelfieCaptureScreenContent(
    agentMode: Boolean = false,
    manualCaptureMode: Boolean = false,
    viewModel: SelfieViewModel = viewModel(),
    onResult: SelfieCaptureResultCallback = SelfieCaptureResultCallback {},
) {
    val uiState = viewModel.uiState.collectAsState().value
    val cameraState = rememberCameraState()
    var camSelector by rememberCamSelector(CamSelector.Front)
    val imageAnalyzer = cameraState.rememberImageAnalyzer(
        analyze = { viewModel.analyzeImage(it, onResult) },
        // Guarantees only one image will be delivered for analysis at a time
        imageAnalysisBackpressureStrategy = KeepOnlyLatest,
    )
    val viewfinderSize = min(LocalConfiguration.current.screenWidthDp.dp * (2 / 3f), 256.dp)
    val progressStrokeWidth = 8.dp
    val progressBarSize = viewfinderSize + progressStrokeWidth * 2
    // TODO: Replace hardcoded colors with themes
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            // Force use a white background in order to light up the user's face
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Top Aligned Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            val isAgentModeEnabled = camSelector == CamSelector.Back
            if (agentMode) {
                val agentModeBackgroundColor = if (isAgentModeEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Gray
                }.copy(alpha = 0.25f)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(8.dp))
                        .background(agentModeBackgroundColor)
                        .padding(8.dp, 0.dp),
                ) {
                    Text(text = stringResource(R.string.si_agent_mode), color = Color.Black)
                    Switch(
                        modifier = Modifier.testTag("agentModeSwitch"),
                        checked = isAgentModeEnabled,
                        onCheckedChange = { camSelector = camSelector.inverse },
                        thumbContent = {
                            val contentDescription =
                                stringResource(R.string.si_cd_agent_mode_enabled)
                            if (isAgentModeEnabled) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = contentDescription,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                    )
                }
            }

            // Display only this shape in the Preview -- however, capture the whole image. This is
            // so that the user only sees their face but captures the whole scene, which may provide
            // additional information for the verification process/identifying fraud
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                CameraPreview(
                    cameraState = cameraState,
                    camSelector = camSelector,
                    implementationMode = ImplementationMode.Compatible,
                    imageAnalyzer = imageAnalyzer,
                    isImageAnalysisEnabled = true,
                    // TODO: We should use FitCenter and crop rather than Fill?
                    // scaleType = ScaleType.FitCenter,
                    scaleType = ScaleType.FillCenter,
                    modifier = Modifier
                        .size(viewfinderSize)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .testTag("cameraPreview"),
                )
                val animatedProgress = animateFloatAsState(
                    targetValue = uiState.progress,
                    animationSpec = tween(),
                ).value
                CircularProgressIndicator(
                    progress = animatedProgress,
                    strokeWidth = progressStrokeWidth,
                    modifier = Modifier
                        .size(progressBarSize)
                        .align(Alignment.Center),
                )
            }
        }

        // Bottom Aligned Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = stringResource(uiState.currentDirective),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(8.dp),
            )

            if (manualCaptureMode && cameraState.isInitialized && !uiState.isCapturing) {
                Button(
                    modifier = Modifier
                        .padding(8.dp)
                        .testTag("takePictureButton"),
                    onClick = { viewModel.takeButtonInitiatedPictures(cameraState, onResult) },
                ) { Text("Take Picture") }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally),
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.si_cd_selfie_capture_instructions),
                    // The icon color must be forced to black due to white background
                    tint = Color.Black,
                )
                Text(
                    text = stringResource(R.string.si_selfie_capture_instructions),
                    // The text color must be forced to black due to white background
                    color = Color.Black,
                )
            }
            SmileIdentityAttribution()
        }
    }
}
