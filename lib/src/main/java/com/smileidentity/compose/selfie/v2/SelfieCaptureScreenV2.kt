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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.smileidentity.R
import com.smileidentity.SmileIDOptIn
import com.smileidentity.compose.components.Face
import com.smileidentity.compose.components.FaceAnimatingLeft
import com.smileidentity.compose.components.FaceAnimatingRight
import com.smileidentity.compose.components.FaceAnimatingUp
import com.smileidentity.compose.components.FaceMovingBack
import com.smileidentity.compose.components.FaceMovingCloser
import com.smileidentity.compose.components.ForceBrightness
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.ml.SelfieQualityModel
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.toast
import com.smileidentity.viewmodel.SelfieHint
import com.smileidentity.viewmodel.SelfieState
import com.smileidentity.viewmodel.SmartSelfieV2ViewModel
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
 * Orchestrates the Selfie Capture Flow. Navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 *
 * @param userId The user ID to associate with the selfie capture
 * @param selfieQualityModel The model to use for selfie quality analysis
 * @param modifier The modifier to apply to this composable
 * @param useStrictMode Whether to use strict mode for the selfie capture. Strict mode entails the
 * user performing an active liveness task
 * @param extraPartnerParams Extra partner_params to send to the API
 * @param onResult The callback to invoke when the selfie capture is complete
 */
@OptIn(ExperimentalPermissionsApi::class)
@SmileIDOptIn
@Composable
fun OrchestratedSelfieCaptureScreenV2(
    userId: String,
    selfieQualityModel: SelfieQualityModel,
    modifier: Modifier = Modifier,
    useStrictMode: Boolean = false,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    onResult: SmileIDCallback<SmartSelfieResult> = {},
    @Suppress("UNUSED_PARAMETER") viewModel: SmartSelfieV2ViewModel = viewModel(
        initializer = {
            SmartSelfieV2ViewModel(
                userId = userId,
                useStrictMode = useStrictMode,
                extraPartnerParams = extraPartnerParams,
                selfieQualityModel = selfieQualityModel,
                onResult = onResult,
            )
        },
    ),
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (!granted) {
            // We don't jump to the settings screen here (unlike in CameraPermissionButton)
            // because it would cause an infinite loop of permission requests due to the
            // LaunchedEffect requesting the permission again.
            onResult(SmileIDResult.Error(OperationCanceledException("Camera Permission Denied")))
        }
    }
    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
        if (permissionState.status.shouldShowRationale) {
            context.toast(R.string.si_camera_permission_rationale)
        }
    }
    SmartSelfieV2Screen(modifier = modifier)
    BackHandler { onResult(SmileIDResult.Error(OperationCanceledException("User Cancelled"))) }
}

/**
 * This component is a Camera Preview overlaid with feedback hints and cutout. The overlay changes
 * provide hints to the user about the status of their selfie capture without using text by using
 * color and animation
 */
@Composable
private fun SmartSelfieV2Screen(
    modifier: Modifier = Modifier,
    viewModel: SmartSelfieV2ViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraState = rememberCameraState()
    val camSelector by rememberCamSelector(CamSelector.Front)
    // Force maximum brightness in order to light up the user's face
    ForceBrightness()
    SmartSelfieV2ScreenScaffold(
        modifier = modifier,
        directiveVisual = {
            val size = Modifier.size(64.dp)
            when (val selfieState = uiState.selfieState) {
                is SelfieState.Analyzing -> when (val hint = selfieState.hint) {
                    SelfieHint.NeedLight -> AnimatedImageFromSelfieHint(hint, modifier = size)
                    SelfieHint.SearchingForFace -> AnimatedImageFromSelfieHint(
                        hint,
                        modifier = size,
                    )

                    SelfieHint.OnlyOneFace -> Face(modifier = size)
                    SelfieHint.EnsureEntireFaceVisible -> Face(modifier = size)
                    SelfieHint.PoorImageQuality -> AnimatedImageFromSelfieHint(
                        hint,
                        modifier = size,
                    )

                    SelfieHint.LookLeft -> FaceAnimatingLeft(modifier = size)
                    SelfieHint.LookRight -> FaceAnimatingRight(modifier = size)
                    SelfieHint.LookUp -> FaceAnimatingUp(modifier = size)
                    SelfieHint.MoveBack -> FaceMovingBack(modifier = size)
                    SelfieHint.MoveCloser -> FaceMovingCloser(modifier = size)
                    SelfieHint.LookStraight -> Face(modifier = size)
                }

                SelfieState.Processing -> CircularProgressIndicator(modifier = size)
                is SelfieState.Error -> Image(
                    painter = painterResource(R.drawable.si_processing_error),
                    contentDescription = null,
                    modifier = size,
                )

                is SelfieState.Success -> Image(
                    painter = painterResource(R.drawable.si_processing_success),
                    contentDescription = null,
                    modifier = size,
                )
            }
        },
        directiveText = when (val selfieState = uiState.selfieState) {
            is SelfieState.Analyzing -> stringResource(selfieState.hint.text)
            SelfieState.Processing -> stringResource(R.string.si_smart_selfie_processing_title)
            is SelfieState.Error -> stringResource(R.string.si_smart_selfie_processing_error_title)
            is SelfieState.Success -> stringResource(
                R.string.si_smart_selfie_processing_success_title,
            )
        },
    ) {
        CameraPreview(
            cameraState = cameraState,
            camSelector = camSelector,
            implementationMode = ImplementationMode.Compatible,
            scaleType = ScaleType.FillCenter,
            imageAnalyzer = cameraState.rememberImageAnalyzer(analyze = viewModel::analyzeImage),
            isImageAnalysisEnabled = true,
            modifier = Modifier
                .padding(32.dp)
                .size(384.dp)
                .clip(RoundedCornerShape(32.dp))
                .clipToBounds()
                .border(4.dp, Color.Black, RoundedCornerShape(32.dp))
                .scale(1.1f),
        )
        if (uiState.selfieState is SelfieState.Processing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
            )
        }
    }
}

@Composable
fun SmartSelfieV2ScreenScaffold(
    directiveVisual: @Composable () -> Unit,
    directiveText: String,
    modifier: Modifier = Modifier,
    cameraPreview: @Composable BoxScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
    ) {
        directiveVisual()
        Text(
            text = directiveText,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(24.dp),
        )
        val roundedCornerShape = RoundedCornerShape(32.dp)
        Box(
            content = cameraPreview,
            modifier = Modifier
                .padding(32.dp)
                .aspectRatio(0.75f) // 480 x 640 -> 3/4 -> 0.75
                .clip(roundedCornerShape)
                .clipToBounds()
                .border(8.dp, Color.Black, roundedCornerShape)
                .scale(1.3f),
        )
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
private fun AnimatedImageFromSelfieHint(selfieHint: SelfieHint, modifier: Modifier = Modifier) {
    var atEnd by remember(selfieHint) { mutableStateOf(false) }
    // The extra key() is needed otherwise there are weird artifacts
    // see: https://stackoverflow.com/a/71123697
    val painter = key(selfieHint) {
        rememberAnimatedVectorPainter(
            animatedImageVector = AnimatedImageVector.animatedVectorResource(
                selfieHint.animation,
            ),
            atEnd = atEnd,
        )
    }
    LaunchedEffect(selfieHint) {
        // This is how you start the animation
        atEnd = !atEnd
    }
    Image(
        painter = key(painter) { painter },
        contentDescription = null,
        modifier = modifier,
    )
}

@SmilePreviews
@Composable
private fun SmartSelfieV2ScreenPreview() {
    Preview {
        SmartSelfieV2ScreenScaffold(
            directiveVisual = {
                Image(
                    painter = painterResource(R.drawable.si_processing_success),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                )
            },
            directiveText = "Testing",
            cameraPreview = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Green),
                )
            },
        )
    }
}
