package com.smileidentity.compose.selfie.v2

import android.Manifest
import android.os.OperationCanceledException
import androidx.activity.compose.BackHandler
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.smileidentity.R
import com.smileidentity.SmileIDOptIn
import com.smileidentity.compose.components.ForceBrightness
import com.smileidentity.ml.SelfieQualityModel
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.toast
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

const val DEFAULT_CUTOUT_PROPORTION = 0.8f

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
@OptIn(ExperimentalAnimationGraphicsApi::class)
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
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize().background(Color.White).padding(16.dp),
    ) {
        // TODO: Image
        Image(
            painter = painterResource(R.drawable.si_processing_success),
            contentDescription = null,
            modifier = Modifier.height(64.dp),
        )
        Text("Testing")
        uiState.faceDirectionHint?.let { Text("Look $it", color = Color.Black) }
        val borderColor = if (uiState.showBorderHighlight) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.background
        }
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
        ) {
            if (uiState.showLoading) {
                CircularProgressIndicator()
            }
        }
        // val targetCutoutProportion = when {
        //     uiState.showCompletion || uiState.showLoading -> 0f
        //     uiState.showBorderHighlight -> 0.7f
        //     else -> DEFAULT_CUTOUT_PROPORTION
        // }
        // val cutoutProportion by if (targetCutoutProportion == DEFAULT_CUTOUT_PROPORTION) {
        //     val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
        //     infiniteTransition.animateFloat(
        //         initialValue = DEFAULT_CUTOUT_PROPORTION,
        //         targetValue = DEFAULT_CUTOUT_PROPORTION - 0.03f,
        //         label = "breathingCutoutProportion",
        //         animationSpec = infiniteRepeatable(
        //             animation = tween(durationMillis = 1500, easing = EaseInOut),
        //             repeatMode = RepeatMode.Reverse,
        //         ),
        //     )
        // } else {
        //     animateFloatAsState(
        //         targetValue = targetCutoutProportion,
        //         label = "cutoutProportion",
        //         animationSpec = tween(durationMillis = 500),
        //     )
        // }
        // val selfieHint = uiState.selfieHint
        // val overlayImage = when {
        //     uiState.showCompletion -> painterResource(R.drawable.si_processing_success)
        //     selfieHint != null -> {
        //         var atEnd by remember(selfieHint) { mutableStateOf(false) }
        //         // The extra key() is needed otherwise there are weird artifacts
        //         // see: https://stackoverflow.com/a/71123697
        //         val painter = key(selfieHint) {
        //             rememberAnimatedVectorPainter(
        //                 animatedImageVector = AnimatedImageVector.animatedVectorResource(
        //                     selfieHint.animation,
        //                 ),
        //                 atEnd = atEnd,
        //             )
        //         }
        //         LaunchedEffect(selfieHint) {
        //             // This is how you start the animation
        //             atEnd = !atEnd
        //         }
        //         painter
        //     }
        //     else -> null
        // }
        // val backgroundOpacity by animateFloatAsState(
        //     targetValue = uiState.backgroundOpacity,
        //     label = "backgroundOpacity",
        // )
        // val cutoutOpacity by animateFloatAsState(
        //     targetValue = uiState.cutoutOpacity,
        //     label = "cutoutOpacity",
        //     animationSpec = tween(durationMillis = 500),
        // )
        // val cornerBorderColor by animateColorAsState(
        //     targetValue = borderColor,
        //     label = "cornerBorderColor",
        // )
        // Canvas(
        //     modifier = Modifier
        //         .fillMaxSize()
        //         // This is what allows the cutout to subtract properly
        //         .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        // ) {
        //     // The main background
        //     drawRect(Color.Black.copy(alpha = backgroundOpacity))
        //
        //     val roundedRectSize = cutoutProportion * size
        //     val radius = CornerRadius(16.dp.toPx())
        //     val roundedRectTopLeft = Offset(
        //         x = (size.width - roundedRectSize.width) / 2.0f,
        //         y = (size.height - roundedRectSize.height) / 2.0f,
        //     )
        //
        //     // Draw the cutout
        //     drawRoundRect(
        //         cornerRadius = radius,
        //         size = roundedRectSize,
        //         topLeft = roundedRectTopLeft,
        //         color = Color.Black.copy(alpha = cutoutOpacity),
        //         style = Fill,
        //         blendMode = BlendMode.SrcIn,
        //     )
        //
        //     // Draw the corner borders
        //     // We draw a Path here and add a RoundedRect as opposed to drawing a RoundedRect
        //     // directly with a similar dashed border. This is because of differences in Skia
        //     // rendering between different Android versions.
        //     // see: https://kotlinlang.slack.com/archives/C04TPPEQKEJ/p1709679738650129
        //
        //     if (cutoutProportion > 0) {
        //         val roundedRect = RoundRect(
        //             rect = Rect(offset = roundedRectTopLeft, size = roundedRectSize),
        //             cornerRadius = radius,
        //         )
        //         drawPath(
        //             path = Path().apply { addRoundRect(roundedRect) },
        //             color = cornerBorderColor,
        //             style = Stroke(
        //                 width = 4.dp.toPx(),
        //                 cap = StrokeCap.Round,
        //                 pathEffect = roundedRectCornerDashPathEffect(
        //                     cornerRadius = radius.x,
        //                     roundedRectSize = roundedRectSize,
        //                     extendCornerBy = 16.dp.toPx(),
        //                 ),
        //             ),
        //         )
        //     }
        // }
        //
        // AnimatedVisibility(
        //     visible = overlayImage != null,
        //     enter = fadeIn() + expandIn(expandFrom = Center),
        // ) {
        //     // TODO: Add Lottie animations here
        //     overlayImage?.let {
        //         Image(
        //             // The extra key() is needed otherwise there are weird artifacts
        //             // see: https://stackoverflow.com/a/71123697
        //             painter = key(overlayImage) { overlayImage },
        //             contentDescription = null,
        //         )
        //     }
        // }
    }
}
