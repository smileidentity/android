package com.smileidentity.compose.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smileidentity.compose.preview.Preview
import kotlin.math.min
import kotlin.math.sin

@Composable
fun Face(
    modifier: Modifier = Modifier,
    featureOffsetX: Float = 0.5f,
    featureOffsetY: Float = 0.5f,
) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val strokeWidth = 2.dp.toPx()
        drawCircle(
            color = color,
            radius = size.minDimension / 2,
            center = Offset(size.width / 2, size.height / 2),
            style = Stroke(width = strokeWidth),
        )

        val left = size.width * (featureOffsetX - 0.5f)
        val top = size.height * (featureOffsetY - 0.5f)
        translate(left, top) {
            // Left Eye. Shrinks slightly when featureOffsetX is < 0.5
            // When featureOffsetX is 0, the eye is at its smallest, at 1/4 the original height
            // If featureOffsetX is > 0.5, then no change
            val leftEyeStart = Offset(size.width / 3, size.height / 3)
            val leftEyeEnd = Offset(size.width / 3, size.height / 2)
            val leftEyeHeight = leftEyeEnd - leftEyeStart
            val normalizedLeftEyeHeight = leftEyeHeight * min(
                sin(featureOffsetX.coerceAtMost(0.5f) * Math.PI.toFloat()),
                sin(featureOffsetY * Math.PI.toFloat()),
            )
            val normalizedLeftEyeOffset = (leftEyeHeight - normalizedLeftEyeHeight) / 2f
            val normalizedLeftEyeStart = leftEyeStart + normalizedLeftEyeOffset
            val normalizedLeftEyeEnd = leftEyeEnd - normalizedLeftEyeOffset
            drawLine(
                color = color,
                start = normalizedLeftEyeStart,
                end = normalizedLeftEyeEnd,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )

            // Right Eye
            val rightEyeStart = Offset(size.width * 2 / 3, size.height / 3)
            val rightEyeEnd = Offset(size.width * 2 / 3, size.height / 2)
            val rightEyeHeight = rightEyeEnd - rightEyeStart
            val normalizedRightEyeHeight = rightEyeHeight * min(
                sin(featureOffsetX.coerceAtLeast(0.5f) * Math.PI.toFloat()),
                sin(featureOffsetY * Math.PI.toFloat()),
            )
            val normalizedRightEyeOffset = (rightEyeHeight - normalizedRightEyeHeight) / 2f
            val normalizedRightEyeStart = rightEyeStart + normalizedRightEyeOffset
            val normalizedRightEyeEnd = rightEyeEnd - normalizedRightEyeOffset
            drawLine(
                color = color,
                start = normalizedRightEyeStart,
                end = normalizedRightEyeEnd,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )

            // Mouth
            val mouthStart = Offset(size.width * 0.4f, size.height * 2 / 3)
            val mouthEnd = Offset(size.width * 0.6f, size.height * 2 / 3)
            val mouthWidth = mouthEnd - mouthStart
            // val widthScaleFactor = 0.5f * sin(featureOffsetX / 0.5f * Math.PI.toFloat()) + 0.5f
            val normalizedMouthWidth = mouthWidth * min(
                sin(featureOffsetX * Math.PI.toFloat()),
                sin(featureOffsetY * Math.PI.toFloat()),
            )
            val normalizedMouthOffset = (mouthWidth - normalizedMouthWidth) / 2f
            val normalizedMouthStart = mouthStart + normalizedMouthOffset
            val normalizedMouthEnd = mouthEnd - normalizedMouthOffset
            drawLine(
                color = color,
                start = normalizedMouthStart,
                end = normalizedMouthEnd,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun FaceAnimatingLeft(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "FaceAnimatingLeft")
    val featureOffsetX by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.5f,
        label = "FaceAnimatingLeft.featureOffsetX",
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Face(modifier = modifier, featureOffsetX = featureOffsetX)
}

@Composable
fun FaceAnimatingRight(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "FaceAnimatingRight")
    val featureOffsetX by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.75f,
        label = "FaceAnimatingRight.featureOffsetX",
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Face(modifier = modifier, featureOffsetX = featureOffsetX)
}

@Composable
fun FaceAnimatingUp(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "FaceAnimatingUp")
    val featureOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.25f,
        label = "FaceAnimatingUp.featureOffsetY",
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Face(modifier = modifier, featureOffsetY = featureOffsetY)
}

@Preview
@Composable
private fun FacePreview() {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val featureOffsetX by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.75f,
        label = "featureOffsetX",
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val featureOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.25f,
        label = "featureOffsetY",
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOut),
            repeatMode = RepeatMode.Restart,
        ),
    )
    Preview {
        Surface {
            Face(
                featureOffsetX = featureOffsetX,
                featureOffsetY = featureOffsetY,
                modifier = Modifier.size(128.dp),
            )
        }
    }
}
