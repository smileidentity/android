package com.smileidentity.compose.components

import android.graphics.BitmapFactory
import androidx.annotation.FloatRange
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.smileidentity.compose.preview.Preview
import com.smileidentity.viewmodel.SelfieHint
import com.smileidentity.viewmodel.SelfieState
import com.smileidentity.viewmodel.SmartSelfieV2UiState
import java.io.File
import kotlin.math.roundToInt

@Composable
fun OvalCutout(
    @FloatRange(from = 0.0, to = 1.0) faceFillPercent: Float,
    state: SmartSelfieV2UiState,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    progressStrokeWidth: Dp = 10.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.scrim,
    arcBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    arcColor: Color = MaterialTheme.colorScheme.tertiary,
    selfieFile: File? = null,
) {
    val color = when (state.selfieState) {
        is SelfieState.Analyzing -> {
            when (state.selfieState.hint) {
                SelfieHint.NeedLight, SelfieHint.SearchingForFace, SelfieHint.MoveBack,
                SelfieHint.MoveCloser, SelfieHint.EnsureDeviceUpright, SelfieHint.OnlyOneFace,
                SelfieHint.EnsureEntireFaceVisible, SelfieHint.PoorImageQuality,
                SelfieHint.LookStraight,
                    -> MaterialTheme.colorScheme.errorContainer

                SelfieHint.LookLeft, SelfieHint.LookRight,
                SelfieHint.LookUp,
                    -> MaterialTheme.colorScheme.tertiary
            }
        }

        is SelfieState.Error -> MaterialTheme.colorScheme.errorContainer
        SelfieState.Processing -> MaterialTheme.colorScheme.tertiary
        is SelfieState.Success -> MaterialTheme.colorScheme.tertiary
    }

    val selfieBitmap = remember(selfieFile) {
        selfieFile?.let {
            BitmapFactory.decodeFile(it.absolutePath)
        }
    }

    val progressAnimationSpec = tween<Float>(
        durationMillis = 200,
        easing = FastOutSlowInEasing,
    )

    val topProgress by animateFloatAsState(
        targetValue = state.topProgress,
        animationSpec = progressAnimationSpec,
        label = "selfie_top_progress",
    )

    val rightProgress by animateFloatAsState(
        targetValue = state.rightProgress,
        animationSpec = progressAnimationSpec,
        label = "selfie_right_progress",
    )

    val leftProgress by animateFloatAsState(
        targetValue = state.leftProgress,
        animationSpec = progressAnimationSpec,
        label = "selfie_left_progress",
    )

    Canvas(modifier.fillMaxSize()) {
        val ovalAspectRatio = 480f / 640f
        val newSize = size * faceFillPercent
        // Constrain either the height or the width of newSize to match ovalAspectRatio
        val newAspectRatio = newSize.width / newSize.height
        val constrainedSize = if (newAspectRatio > ovalAspectRatio) {
            Size(width = newSize.height * ovalAspectRatio, height = newSize.height)
        } else {
            Size(width = newSize.width, height = newSize.width / ovalAspectRatio)
        }

        val ovalOffset = Offset(
            x = (size.width - constrainedSize.width) / 2,
            y = (size.height - constrainedSize.height) / 2,
        )

        val ovalPath = Path().apply {
            addOval(
                Rect(
                    size = constrainedSize,
                    offset = ovalOffset,
                ),
            )
        }

        if (selfieBitmap != null && (
                state.selfieState is SelfieState.Processing ||
                    state.selfieState is SelfieState.Success
                )
        ) {
            clipPath(ovalPath) {
                drawImage(
                    image = selfieBitmap.asImageBitmap(),
                    dstSize = IntSize(
                        width = constrainedSize.width.roundToInt(),
                        height = constrainedSize.height.roundToInt(),
                    ),
                    dstOffset = IntOffset(
                        x = ovalOffset.x.roundToInt(),
                        y = ovalOffset.y.roundToInt(),
                    ),
                )
            }
        }

        clipPath(ovalPath, clipOp = ClipOp.Difference) {
            drawRect(color = backgroundColor)
        }

        drawPath(
            path = ovalPath,
            color = color,
            style = Stroke(width = strokeWidth.toPx()),
        )

        if (state.selfieState is SelfieState.Analyzing) {
            val arcWidth = constrainedSize.width * 0.6f
            val arcHeight = constrainedSize.height * 0.6f

            val arcCenter = Offset(
                x = ovalOffset.x + constrainedSize.width / 2,
                y = ovalOffset.y + constrainedSize.height / 2,
            )

            val arcSize = Size(width = arcWidth * 2, height = arcHeight * 2)
            val arcStroke = Stroke(width = progressStrokeWidth.toPx(), cap = StrokeCap.Round)
            val arcTopLeft = Offset(
                x = arcCenter.x - arcWidth,
                y = arcCenter.y - arcHeight,
            )

            when (state.selfieState.hint) {
                SelfieHint.LookLeft -> {
                    drawArc(
                        color = arcBackgroundColor,
                        startAngle = 150f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = arcStroke,
                    )
                    drawArc(
                        color = arcColor,
                        startAngle = 150f,
                        sweepAngle = 60f * leftProgress,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = arcStroke,
                    )
                }

                SelfieHint.LookRight -> {
                    drawArc(
                        color = arcBackgroundColor,
                        startAngle = -30f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = arcStroke,
                    )
                    drawArc(
                        color = arcColor,
                        startAngle = 30f,
                        sweepAngle = -60f * rightProgress,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = arcStroke,
                    )
                }

                SelfieHint.LookUp -> {
                    drawArc(
                        color = arcBackgroundColor,
                        startAngle = 245f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = arcStroke,
                    )
                    drawArc(
                        color = arcColor,
                        startAngle = 245f,
                        sweepAngle = 60f * topProgress,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = arcStroke,
                    )
                }

                else -> {}
            }
        }
    }

    DisposableEffect(selfieBitmap) {
        onDispose {
            selfieBitmap?.recycle()
        }
    }
}

@Composable
@Preview
private fun OvalCutoutPreview() {
    Preview {
        OvalCutout(
            faceFillPercent = 0.45F,
            state = SmartSelfieV2UiState(
                rightProgress = 0.5F,
                selfieState = SelfieState.Analyzing(SelfieHint.LookRight),
            ),
        )
    }
}
