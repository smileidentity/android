package com.smileidentity.compose.selfie.enhanced

import android.Manifest
import android.os.OperationCanceledException
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.smileidentity.R
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.compose.components.CameraPermissionButton
import com.smileidentity.compose.components.DirectiveHaptics
import com.smileidentity.compose.components.DirectiveVisual
import com.smileidentity.compose.components.ForceBrightness
import com.smileidentity.compose.components.OvalCutout
import com.smileidentity.compose.components.SmileIDAttribution
import com.smileidentity.compose.components.cameraFrameCornerBorder
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.ml.SelfieQualityModel
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.toast
import com.smileidentity.viewmodel.MAX_FACE_AREA_THRESHOLD
import com.smileidentity.viewmodel.SelfieHint
import com.smileidentity.viewmodel.SelfieState
import com.smileidentity.viewmodel.SmartSelfieEnhancedViewModel
import com.smileidentity.viewmodel.SmartSelfieV2UiState
import com.smileidentity.viewmodel.VIEWFINDER_SCALE
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.ImplementationMode
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import com.ujizin.camposer.state.rememberImageAnalyzer
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * Orchestrates the Selfie Capture Flow. Requests permissions, sets brightness, handles back press,
 * shows the view, and handling the viewmodel.
 *
 * @param userId The user ID to associate with the selfie capture
 * @param isEnroll Whether this selfie capture is for enrollment
 * @param selfieQualityModel The model to use for selfie quality analysis
 * @param onResult The callback to invoke when the selfie capture is complete
 * @param modifier The modifier to apply to this composable
 * about what constitutes a good selfie capture and results in better pass rates.
 * @param showAttribution Whether to show the Smile ID attribution
 * @param allowNewEnroll Whether to allow new enrollments
 * @param extraPartnerParams Extra partner_params to send to the API
 * @param viewModel The viewmodel to use for te selfie capture (should not be explicitly passed in)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OrchestratedSelfieCaptureScreenEnhanced(
    userId: String,
    isEnroll: Boolean,
    selfieQualityModel: SelfieQualityModel,
    onResult: SmileIDCallback<SmartSelfieResult>,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    allowNewEnroll: Boolean? = null,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    skipApiSubmission: Boolean = false,
    viewModel: SmartSelfieEnhancedViewModel = viewModel(
        initializer = {
            SmartSelfieEnhancedViewModel(
                userId = userId,
                isEnroll = isEnroll,
                allowNewEnroll = allowNewEnroll,
                extraPartnerParams = extraPartnerParams,
                selfieQualityModel = selfieQualityModel,
                skipApiSubmission = skipApiSubmission,
                onResult = onResult,
            )
        },
    ),
) {
    BackHandler { onResult(SmileIDResult.Error(OperationCanceledException("User cancelled"))) }
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (!granted) {
            // We don't jump to the settings screen here (unlike in CameraPermissionButton)
            // because it would cause an infinite loop of permission requests due to the
            // LaunchedEffect requesting the permission again. We should leave this decision to the
            // caller.
            onResult(SmileIDResult.Error(OperationCanceledException("Camera permission denied")))
        }
    }
    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
        if (permissionState.status.shouldShowRationale) {
            context.toast(R.string.si_camera_permission_rationale)
        }
    }
    ForceBrightness()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var acknowledgedInstructions by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .background(color = Color.White)
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        val cameraState = rememberCameraState()
        val camSelector by rememberCamSelector(CamSelector.Front)

        when {
            showInstructions && !acknowledgedInstructions -> SelfieCaptureInstructionScreenEnhanced(
                modifier = Modifier.fillMaxSize(),
            ) {
                acknowledgedInstructions = true
            }

            else -> SmartSelfieEnhancedScreen(
                state = uiState,
                showAttribution = showAttribution,
                modifier = modifier,
                onRetry = viewModel::onRetry,
                onResult = onResult,
                cameraPreview = {
                    CameraPreview(
                        cameraState = cameraState,
                        camSelector = camSelector,
                        implementationMode = ImplementationMode.Compatible,
                        scaleType = ScaleType.FillCenter,
                        imageAnalyzer = cameraState.rememberImageAnalyzer(
                            analyze = {
                                viewModel.analyzeImage(
                                    imageProxy = it,
                                    camSelector = camSelector,
                                )
                            },
                        ),
                        isImageAnalysisEnabled = true,
                        modifier = Modifier
                            .padding(12.dp)
                            .scale(VIEWFINDER_SCALE),
                    )
                },
            )
        }
    }
}

/**
 * The Smart Selfie Capture Screen. This screen is responsible for displaying the selfie capture
 * contents, including directive visual, directive text, camera preview, retry/close buttons,
 * attribution, and agent mode switch.
 * This composable relies on the caller to make camera changes and perform image analysis.
 *
 * @param selfieState The state of the selfie capture
 * @param onRetry The callback to invoke when the user wants to retry on error
 * @param onResult The callback to invoke when the selfie capture is complete
 * @param cameraPreview The composable slot to display the camera preview
 * @param modifier The modifier to apply to this composable
 * @param showAttribution Whether to show the Smile ID attribution
 */
@Composable
private fun SmartSelfieEnhancedScreen(
    state: SmartSelfieV2UiState,
    onRetry: () -> Unit,
    onResult: SmileIDCallback<SmartSelfieResult>,
    cameraPreview: @Composable (BoxScope.() -> Unit),
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
) {
    ForceBrightness()
    val viewfinderZoom = 1.1f
    val faceFillPercent = remember { MAX_FACE_AREA_THRESHOLD * viewfinderZoom * 2 }
    BottomPinnedColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(16.dp),
        scrollableContent = {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .fillMaxSize()
                    .height(IntrinsicSize.Min),
            ) {
                DirectiveHaptics(selfieState = state.selfieState)

                val roundedCornerShape = RoundedCornerShape(32.dp)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .aspectRatio(0.75f) // 480 x 640 -> 3/4 -> 0.75
                        .clip(roundedCornerShape)
                        .drawWithCache {
                            val roundRect = RoundRect(
                                rect = size.toRect(),
                                cornerRadius = CornerRadius(32.dp.toPx()),
                            )
                            onDrawWithContent {
                                drawContent()
                                drawPath(
                                    path = Path().apply { addRoundRect(roundRect = roundRect) },
                                    color = Color.Transparent,
                                    style = Stroke(width = 20.dp.toPx()),
                                )
                                cameraFrameCornerBorder(
                                    cornerRadius = 32.dp.toPx(),
                                    strokeWidth = 20.dp.toPx(),
                                )
                                drawPath(
                                    path = Path().apply { addRoundRect(roundRect = roundRect) },
                                    color = Color.Transparent,
                                    style = Stroke(width = 12.dp.toPx()),
                                )
                            }
                        }
                        .weight(1f, fill = false),
                ) {
                    cameraPreview()

                    Box(
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        OvalCutout(
                            faceFillPercent = faceFillPercent,
                            state = state,
                            selfieFile = when (state.selfieState) {
                                is SelfieState.Processing -> state.selfieFile
                                is SelfieState.Success -> state.selfieState.selfieFile
                                else -> null
                            },
                            backgroundColor = Color(0xFF2D2B2A).copy(alpha = 0.8f),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("selfie_progress_indicator"),
                        )

                        when (state.selfieState) {
                            is SelfieState.Analyzing -> {
                                DirectiveVisual(
                                    selfieState = state.selfieState,
                                    modifier = Modifier
                                        .size(150.dp)
                                        .align(Alignment.Center),
                                )
                            }

                            is SelfieState.Error -> {
                                Image(
                                    painter = painterResource(R.drawable.si_selfie_failed),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Center),
                                )
                            }

                            SelfieState.Processing -> {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier
                                        .align(Alignment.Center),
                                )
                            }

                            is SelfieState.Success -> {
                                Image(
                                    painter = painterResource(R.drawable.si_selfie_success),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Center),
                                )
                                onResult(
                                    SmileIDResult.Success(
                                        SmartSelfieResult(
                                            selfieFile = state.selfieState.selfieFile,
                                            livenessFiles = state.selfieState.livenessFiles,
                                            apiResponse = state.selfieState.result,
                                        ),
                                    ),
                                )
                            }
                        }
                        UserInstructionsView(
                            instruction = when (state.selfieState) {
                                is SelfieState.Analyzing -> stringResource(
                                    state.selfieState.hint.text,
                                )

                                SelfieState.Processing -> stringResource(
                                    R.string.si_smart_selfie_enhanced_submitting,
                                )

                                is SelfieState.Error -> stringResource(
                                    R.string.si_smart_selfie_enhanced_submission_failed,
                                )

                                is SelfieState.Success -> stringResource(
                                    R.string.si_smart_selfie_enhanced_submission_successful,
                                )
                            },
                            message = when (state.selfieState) {
                                is SelfieState.Error ->
                                    state.selfieState.throwable.message?.takeIf { it.isNotEmpty() }

                                else -> null
                            },
                        )
                    }
                }
                if (showAttribution) {
                    SmileIDAttribution(modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        pinnedContent = {
            if (state.selfieState is SelfieState.Error) {
                CameraPermissionButton(
                    text = stringResource(R.string.si_smart_selfie_processing_retry_button),
                    modifier = Modifier.width(320.dp),
                    onGranted = onRetry,
                )

                TextButton(
                    onClick = {
                        onResult(
                            SmileIDResult.Error(OperationCanceledException("User cancelled")),
                        )
                    },
                    modifier = Modifier
                        .testTag("selfie_screen_cancel_button")
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(id = R.string.si_cancel),
                        color = colorResource(id = R.color.si_color_material_error_container),
                    )
                }
            } else if (state.selfieState is SelfieState.Analyzing) {
                TextButton(
                    onClick = {
                        onResult(
                            SmileIDResult.Error(OperationCanceledException("User cancelled")),
                        )
                    },
                    modifier = Modifier
                        .testTag("selfie_screen_cancel_button")
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(id = R.string.si_cancel),
                        color = colorResource(id = R.color.si_color_material_error_container),
                    )
                }
            }
        },
    )
}

/**
 * The Selfie Capture Instruction Screen. This screen is responsible for displaying the
 * instructions to the user based on the current selfie state.
 * @param instruction Main instruction to display (title)
 * @param message Subtitle message to display (optional)
 * @param modifier The modifier to apply to this composable
 */
@Composable
private fun UserInstructionsView(
    instruction: String,
    message: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = instruction,
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
        message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@SmilePreviews
@Composable
private fun SmartSelfieEnhancedScreenPreview() {
    Preview {
        Column {
            SmartSelfieEnhancedScreen(
                state = SmartSelfieV2UiState(
                    topProgress = 0.8f,
                    rightProgress = 0.5f,
                    leftProgress = 0.3f,
                    selfieState = SelfieState.Analyzing(SelfieHint.LookRight),
                ),
                onResult = {},
                onRetry = {},
                showAttribution = true,
                modifier = Modifier.fillMaxSize(),
                cameraPreview = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray),
                    )
                },
            )
        }
    }
}
