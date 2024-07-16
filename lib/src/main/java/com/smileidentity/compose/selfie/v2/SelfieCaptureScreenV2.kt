package com.smileidentity.compose.selfie.v2

import android.Manifest
import android.os.OperationCanceledException
import androidx.activity.compose.BackHandler
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.smileidentity.compose.components.Face
import com.smileidentity.compose.components.FaceMovingBack
import com.smileidentity.compose.components.FaceMovingCloser
import com.smileidentity.compose.components.ForceBrightness
import com.smileidentity.compose.components.LocalMetadata
import com.smileidentity.compose.components.LottieFace
import com.smileidentity.compose.components.LottieFaceLookingLeft
import com.smileidentity.compose.components.LottieFaceLookingRight
import com.smileidentity.compose.components.LottieFaceLookingUp
import com.smileidentity.compose.components.OvalCutout
import com.smileidentity.compose.components.SmileIDAttribution
import com.smileidentity.compose.components.cameraFrameCornerBorder
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.compose.selfie.AgentModeSwitch
import com.smileidentity.ml.SelfieQualityModel
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.toast
import com.smileidentity.viewmodel.SelfieHint
import com.smileidentity.viewmodel.SelfieState
import com.smileidentity.viewmodel.SmartSelfieV2ViewModel
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
import kotlinx.coroutines.delay

/**
 * Orchestrates the Selfie Capture Flow. Requests permissions, sets brightness, handles back press,
 * shows the view, and handling the viewmodel.
 *
 * @param userId The user ID to associate with the selfie capture
 * @param isEnroll Whether this selfie capture is for enrollment
 * @param selfieQualityModel The model to use for selfie quality analysis
 * @param onResult The callback to invoke when the selfie capture is complete
 * @param modifier The modifier to apply to this composable
 * @param useStrictMode Whether to use strict mode for the selfie capture. Strict mode is stricter
 * about what constitutes a good selfie capture and results in better pass rates.
 * @param showAttribution Whether to show the Smile ID attribution
 * @param allowAgentMode Whether to allow the user to switch to agent mode (back camera)
 * @param allowNewEnroll Whether to allow new enrollments
 * @param extraPartnerParams Extra partner_params to send to the API
 * @param viewModel The viewmodel to use for the selfie capture (should not be explicitly passed in)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OrchestratedSelfieCaptureScreenV2(
    userId: String,
    isEnroll: Boolean,
    selfieQualityModel: SelfieQualityModel,
    onResult: SmileIDCallback<SmartSelfieResult>,
    modifier: Modifier = Modifier,
    useStrictMode: Boolean = false,
    showAttribution: Boolean = true,
    allowAgentMode: Boolean = false,
    allowNewEnroll: Boolean? = null,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    metadata: SnapshotStateList<Metadatum> = LocalMetadata.current,
    viewModel: SmartSelfieV2ViewModel = viewModel(
        initializer = {
            SmartSelfieV2ViewModel(
                userId = userId,
                isEnroll = isEnroll,
                allowNewEnroll = allowNewEnroll,
                useStrictMode = useStrictMode,
                extraPartnerParams = extraPartnerParams,
                selfieQualityModel = selfieQualityModel,
                metadata = metadata,
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
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        val cameraState = rememberCameraState()
        var camSelector by rememberCamSelector(CamSelector.Front)
        SmartSelfieV2Screen(
            selfieState = uiState.selfieState,
            showAttribution = showAttribution,
            allowAgentMode = allowAgentMode,
            isAgentModeEnabled = camSelector == CamSelector.Back,
            onCamSelectorChange = { camSelector = camSelector.inverse },
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
                        analyze = { viewModel.analyzeImage(it, camSelector) },
                    ),
                    isImageAnalysisEnabled = true,
                    modifier = Modifier
                        .padding(32.dp)
                        .scale(VIEWFINDER_SCALE),
                )
            },
        )
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
 * @param isAgentModeEnabled Whether agent mode is enabled
 * @param onCamSelectorChange The callback to invoke when the user wants to switch cameras
 * @param modifier The modifier to apply to this composable
 * @param showAttribution Whether to show the Smile ID attribution
 * @param allowAgentMode Whether to allow the user to switch to agent mode (back camera)
 */
@Composable
fun ColumnScope.SmartSelfieV2Screen(
    selfieState: SelfieState,
    onRetry: () -> Unit,
    onResult: SmileIDCallback<SmartSelfieResult>,
    cameraPreview: @Composable (BoxScope.() -> Unit),
    isAgentModeEnabled: Boolean,
    onCamSelectorChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
    allowAgentMode: Boolean = false,
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(16.dp),
    ) {
        DirectiveHaptics(selfieState)

        // Could be loading indicator, composable animation, animated image, or static image
        DirectiveVisual(
            selfieState = selfieState,
            modifier = Modifier.size(80.dp),
        )
        Text(
            text = when (selfieState) {
                is SelfieState.Analyzing -> stringResource(selfieState.hint.text)
                SelfieState.Processing -> stringResource(R.string.si_smart_selfie_v2_submitting)
                is SelfieState.Error -> stringResource(
                    R.string.si_smart_selfie_v2_submission_failed,
                )

                is SelfieState.Success -> stringResource(
                    R.string.si_smart_selfie_v2_submission_successful,
                )
            },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp),
        )
        val roundedCornerShape = RoundedCornerShape(32.dp)
        val mainBorderColor = MaterialTheme.colorScheme.inverseSurface
        val accentBorderColor = MaterialTheme.colorScheme.tertiary
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .aspectRatio(0.75f) // 480 x 640 -> 3/4 -> 0.75
                .clip(roundedCornerShape)
                // We draw borders as a individual layers in the Box (as opposed to Modifier.border)
                // because we need multiple colors, and eventually we will need to animate them for
                // Active Liveness feedback
                .drawWithCache {
                    val roundRect = RoundRect(size.toRect(), CornerRadius(32.dp.toPx()))
                    onDrawWithContent {
                        drawContent()
                        drawPath(
                            path = Path().apply { addRoundRect(roundRect) },
                            color = mainBorderColor,
                            style = Stroke(width = 20.dp.toPx()),
                        )
                        cameraFrameCornerBorder(
                            cornerRadius = 32.dp.toPx(),
                            strokeWidth = 20.dp.toPx(),
                            color = accentBorderColor,
                        )
                        drawPath(
                            path = Path().apply { addRoundRect(roundRect) },
                            color = mainBorderColor,
                            style = Stroke(width = 12.dp.toPx()),
                        )
                    }
                }
                .weight(1f, fill = false),
        ) {
            cameraPreview()

            if (selfieState !is SelfieState.Analyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                )
            } else {
                OvalCutout(
                    faceFillPercent = 0.6f,
                    backgroundColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f),
                )
            }
        }
        if (selfieState is SelfieState.Error) {
            // Displaying these Buttons may cause a re-layout/element shift on smaller screens.
            // For most screen sizes, it shouldn't. This is so that we can maximize the camera
            // preview size on those smaller screen devices.
            Button(
                onClick = onRetry,
                modifier = Modifier.width(320.dp),
                content = {
                    Text(text = stringResource(R.string.si_smart_selfie_processing_retry_button))
                },
            )
            TextButton(
                onClick = { onResult(SmileIDResult.Error(selfieState.throwable)) },
                modifier = Modifier.width(320.dp),
                content = {
                    Text(text = stringResource(R.string.si_smart_selfie_processing_close_button))
                },
            )
        }
        if (allowAgentMode) {
            AgentModeSwitch(
                isAgentModeEnabled = isAgentModeEnabled,
                onCamSelectorChange = onCamSelectorChange,
            )
        }
        if (showAttribution) {
            SmileIDAttribution(modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun ColumnScope.DirectiveVisual(selfieState: SelfieState, modifier: Modifier = Modifier) {
    when (selfieState) {
        is SelfieState.Analyzing -> when (val hint = selfieState.hint) {
            SelfieHint.NeedLight -> AnimatedImageFromSelfieHint(hint, modifier = modifier)
            SelfieHint.SearchingForFace -> AnimatedImageFromSelfieHint(
                hint,
                modifier = modifier,
            )

            SelfieHint.EnsureDeviceUpright -> AnimatedImageFromSelfieHint(
                hint,
                modifier = modifier,
            )

            SelfieHint.OnlyOneFace -> Face(modifier = modifier)
            SelfieHint.EnsureEntireFaceVisible -> Face(modifier = modifier)
            SelfieHint.PoorImageQuality -> AnimatedImageFromSelfieHint(
                hint,
                modifier = modifier,
            )

            SelfieHint.LookLeft -> LottieFaceLookingLeft(modifier = modifier)
            SelfieHint.LookRight -> LottieFaceLookingRight(modifier = modifier)
            SelfieHint.LookUp -> LottieFaceLookingUp(modifier = modifier)
            SelfieHint.MoveBack -> FaceMovingBack(modifier = modifier)
            SelfieHint.MoveCloser -> FaceMovingCloser(modifier = modifier)
            SelfieHint.LookStraight -> LottieFace(startFrame = 0, endFrame = 0, modifier = modifier)
            SelfieHint.Smile -> LottieFace(startFrame = 0, endFrame = 0, modifier = modifier)
        }

        SelfieState.Processing -> CircularProgressIndicator(modifier = modifier)
        is SelfieState.Error -> Image(
            painter = painterResource(R.drawable.si_error_enclosed_x),
            contentDescription = null,
            modifier = modifier,
        )

        is SelfieState.Success -> Image(
            painter = painterResource(R.drawable.si_processing_success),
            contentDescription = null,
            modifier = modifier,
        )
    }
}

/**
 * Displays the animated image for the given selfie hint.
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
private fun AnimatedImageFromSelfieHint(selfieHint: SelfieHint, modifier: Modifier = Modifier) {
    var atEnd by remember(selfieHint) { mutableStateOf(false) }
    // The extra key() is needed otherwise there are weird artifacts
    // see: https://stackoverflow.com/a/71123697
    val painter = key(selfieHint) {
        rememberAnimatedVectorPainter(
            animatedImageVector = AnimatedImageVector.animatedVectorResource(selfieHint.animation),
            atEnd = atEnd,
        )
    }
    // This is how you start the animation
    LaunchedEffect(selfieHint) { atEnd = !atEnd }
    Image(
        painter = key(painter) { painter },
        contentDescription = null,
        modifier = modifier,
    )
}

/**
 * Provide custom haptic feedback based on the selfie hint.
 */
@Composable
private fun DirectiveHaptics(selfieState: SelfieState) {
    val haptic = LocalHapticFeedback.current
    if (selfieState is SelfieState.Analyzing) {
        if (selfieState.hint == SelfieHint.LookUp ||
            selfieState.hint == SelfieHint.LookRight ||
            selfieState.hint == SelfieHint.LookLeft
        ) {
            LaunchedEffect(selfieState.hint) {
                // Custom vibration pattern
                for (i in 0..2) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    delay(100)
                }
            }
        }
    }
}

@SmilePreviews
@Composable
private fun SmartSelfieV2ScreenPreview() {
    Preview {
        Column {
            SmartSelfieV2Screen(
                // selfieState = SelfieState.Processing,
                // selfieState = SelfieState.Error(RuntimeException()),
                selfieState = SelfieState.Analyzing(SelfieHint.LookUp),
                onResult = {},
                onRetry = {},
                showAttribution = true,
                allowAgentMode = true,
                isAgentModeEnabled = false,
                onCamSelectorChange = {},
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
