package com.smileidentity.compose.transactionfraud

import android.Manifest
import android.os.OperationCanceledException
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.times
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.components.ForceBrightness
import com.smileidentity.compose.components.roundedRectCornerDashPathEffect
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography
import com.smileidentity.ml.ImQualCp20Optimized
import com.smileidentity.models.SmartSelfieJobResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.toast
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OrchestratedTransactionFraudScreen(
    userId: String,
    jobId: String,
    imageQualityModel: ImQualCp20Optimized,
    modifier: Modifier = Modifier,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    onResult: SmileIDCallback<SmartSelfieJobResult.Entry> = {},
    @Suppress("UNUSED_PARAMETER") viewModel: TransactionFraudViewModel = viewModel(
        initializer = {
            TransactionFraudViewModel(
                userId = userId,
                jobId = jobId,
                extraPartnerParams = extraPartnerParams,
                imageQualityModel = imageQualityModel,
                onResult = onResult,
            )
        },
    ),
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (!granted) {
            // We don't show jump to the settings screen here (unlike in CameraPermissionButton)
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
    Dialog(
        onDismissRequest = {
            onResult(SmileIDResult.Error(OperationCanceledException("User Cancelled")))
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        TransactionFraudScreen(
            modifier = modifier
                .height(512.dp)
                .clip(MaterialTheme.shapes.large),
        )
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
private fun TransactionFraudScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionFraudViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraState = rememberCameraState()
    val camSelector by rememberCamSelector(CamSelector.Front)
    // Force maximum brightness in order to light up the user's face
    ForceBrightness()
    Box(contentAlignment = Center, modifier = modifier) {
        CameraPreview(
            cameraState = cameraState,
            camSelector = camSelector,
            implementationMode = ImplementationMode.Compatible,
            scaleType = ScaleType.FillCenter,
            imageAnalyzer = cameraState.rememberImageAnalyzer(analyze = viewModel::analyzeImage),
            isImageAnalysisEnabled = true,
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .scale(1.1f),
        )
        val borderColor = if (uiState.showBorderHighlight) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.background
        }
        val cutoutProportion = when {
            uiState.showCompletion -> 0f
            uiState.showLoading -> 0.2f
            uiState.showBorderHighlight -> 0.7f
            else -> DEFAULT_CUTOUT_PROPORTION
        }
        val animatedCutoutProportion by if (cutoutProportion == DEFAULT_CUTOUT_PROPORTION) {
            val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
            infiniteTransition.animateFloat(
                initialValue = DEFAULT_CUTOUT_PROPORTION,
                targetValue = DEFAULT_CUTOUT_PROPORTION - 0.03f,
                label = "breathingCutoutProportion",
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1500, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse,
                ),
            )
        } else {
            animateFloatAsState(
                targetValue = cutoutProportion,
                label = "cutoutProportion",
                animationSpec = tween(durationMillis = 500),
            )
        }
        val selfieHint = uiState.selfieHint
        val overlayImage = when {
            uiState.showCompletion -> painterResource(R.drawable.si_processing_success)
            selfieHint != null -> {
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
                painter
            }
            else -> null
        }
        FeedbackOverlay(
            backgroundOpacity = animateFloatAsState(
                targetValue = uiState.backgroundOpacity,
                label = "backgroundOpacity",
            ).value,
            cutoutOpacity = animateFloatAsState(
                targetValue = uiState.cutoutOpacity,
                label = "cutoutOpacity",
                animationSpec = tween(durationMillis = 500),
            ).value,
            cutoutProportion = animatedCutoutProportion,
            cornerBorderColor = animateColorAsState(
                targetValue = borderColor,
                label = "cornerBorderColor",
            ).value,
            overlayImage = overlayImage,
        )
        if (uiState.showLoading) {
            CircularProgressIndicator()
        }
    }
}

/**
 * This component is meant to be overlaid over a Camera Preview. The changes in the overlay are
 * meant to provide hints to the user about the status of their selfie capture using color and
 * animation and without using text.
 */
@Composable
private fun FeedbackOverlay(
    backgroundOpacity: Float,
    cutoutOpacity: Float,
    cutoutProportion: Float,
    cornerBorderColor: Color,
    overlayImage: Painter?,
    modifier: Modifier = Modifier,
) = Box(modifier = modifier, contentAlignment = Center) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        drawRect(Color.Black.copy(alpha = backgroundOpacity))
        val radius = 16.dp.toPx()
        // Calculate the width of the non-corner part of the rounded rectangle
        val roundedRectSize = cutoutProportion * size
        // topLeft position such that the entire cutout is centered
        val roundedRectTopLeft = Offset(
            ((1 - cutoutProportion) * size.width) / 2.0f,
            ((1 - cutoutProportion) * size.height) / 2.0f,
        )

        // Draw the rounded rectangle cutout
        drawRoundRect(
            cornerRadius = CornerRadius(radius),
            size = roundedRectSize,
            topLeft = roundedRectTopLeft,
            color = Color.Black.copy(alpha = cutoutOpacity),
            style = Fill,
            blendMode = BlendMode.SrcIn,
        )

        // Draw the corner borders
        // We draw a Path here and add a RoundedRect as opposed to drawing a RoundedRect directly
        // with a similar dashed border. This is because of differences in Skia rendering between
        // different Android versions.
        // see: https://kotlinlang.slack.com/archives/C04TPPEQKEJ/p1709679738650129
        val roundedRect = RoundRect(
            rect = Rect(offset = roundedRectTopLeft, size = roundedRectSize),
            cornerRadius = CornerRadius(radius),
        )
        drawPath(
            path = Path().apply { addRoundRect(roundedRect) },
            color = cornerBorderColor,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = roundedRectCornerDashPathEffect(
                    cornerRadius = radius,
                    roundedRectSize = roundedRectSize,
                    extendCornerBy = 16.dp.toPx(),
                ),
            ),
        )
    }
    AnimatedVisibility(
        visible = overlayImage != null,
        enter = fadeIn() + expandIn(expandFrom = Center),
    ) {
        overlayImage?.let {
            Image(
                // The extra key() is needed otherwise there are weird artifacts
                // see: https://stackoverflow.com/a/71123697
                painter = key(overlayImage) { overlayImage },
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewFeedbackOverlay() {
    MaterialTheme(colorScheme = SmileID.colorScheme, typography = SmileID.typography) {
        Box(modifier = Modifier.background(Color.Gray)) {
            Image(
                painter = painterResource(R.drawable.si_face_outline),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(64.dp),
            )
            FeedbackOverlay(
                backgroundOpacity = 0.8f,
                cutoutOpacity = 0f,
                cutoutProportion = 0.8f,
                cornerBorderColor = MaterialTheme.colorScheme.tertiary,
                overlayImage = null,
            )
        }
    }
}
