package com.smileidentity.compose.components

import androidx.annotation.FloatRange
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.smileidentity.R
import com.smileidentity.compose.preview.Preview
import kotlin.math.min
import kotlin.math.sin

/**
 * Draws a basic face. It can be animated by adjusting the `featureOffsetX` and `featureOffsetY`
 * parameters. NB! Usually you will want to animate between 0.25 and 0.75. Going all the way to
 * 0 or 1 will make the eyes and mouth draw outside the face circle.
 * The eyes and mouth shrink slightly when the head turns, providing a 3D effect.
 *
 * @param modifier The modifier to apply to this layout node.
 * @param featureOffsetX The horizontal offset of the face features. A value of 0.5 means the face
 * is centered, 0 means the face is turned to the left, and 1 means the face is turned to the right.
 * @param featureOffsetY The vertical offset of the face features. A value of 0.5 means the face is
 * centered, 0 means the face is looking up, and 1 means the face is looking down.
 * @param featureScale The scale of the face features. A value of 1 means the face features are
 * normal size, 0.5 means the face features are half the size, and 1.5 means the face features are
 * 50% larger.
 */
@Composable
fun Face(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.25, to = 0.75) featureOffsetX: Float = 0.5f,
    @FloatRange(from = 0.25, to = 0.75) featureOffsetY: Float = 0.5f,
    @FloatRange(from = 0.5, to = 1.5) featureScale: Float = 1f,
) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val strokeWidth = 2.dp.toPx()
        drawCircle(
            color = color,
            radius = size.minDimension / 2,
            center = center,
            style = Stroke(width = strokeWidth),
        )

        scale(featureScale) {
            val left = size.width * (featureOffsetX - 0.5f)
            val top = size.height * (featureOffsetY - 0.5f)
            translate(left, top) {
                // Left Eye. Shrinks when featureOffsetX is < 0.5, or featureOffsetY is ≠ 0.5
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

                // Right Eye. Shrinks when featureOffsetX is > 0.5, or featureOffsetY is ≠ 0.5
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

                // Mouth. Shrinks when featureOffsetX is ≠ 0.5, or featureOffsetY is ≠ 0.5
                val mouthStart = Offset(size.width * 0.4f, size.height * 2 / 3)
                val mouthEnd = Offset(size.width * 0.6f, size.height * 2 / 3)
                val mouthWidth = mouthEnd - mouthStart
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
}

/**
 * Conveniently animated version of the [Face] composable that makes the face look to the left.
 * The animation plays in reverse rather than resetting, and then it repeats indefinitely.
 */
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

/**
 * Conveniently animated version of the [Face] composable that makes the face look to the right.
 * The animation plays in reverse rather than resetting, and then it repeats indefinitely.
 */
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

/**
 * Conveniently animated version of the [Face] composable that makes the face look up.
 * The animation plays in reverse rather than resetting, and then it repeats indefinitely.
 */
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

/**
 * Conveniently animated version of the [Face] composable that makes the face move back.
 * The animation resets rather than playing in reverse, and then it repeats indefinitely.
 */
@Composable
fun FaceMovingBack(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "FaceMovingBack")
    val featureScale by infiniteTransition.animateFloat(
        initialValue = 1.5f,
        targetValue = 1f,
        label = "FaceMovingBack.featureScale",
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOut),
            repeatMode = RepeatMode.Restart,
        ),
    )
    Face(modifier = modifier, featureScale = featureScale)
}

/**
 * Conveniently animated version of the [Face] composable that makes the face move closer.
 * The animation resets rather than playing in reverse, and then it repeats indefinitely.
 */
@Composable
fun FaceMovingCloser(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "FaceMovingForward")
    val featureScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        label = "FaceMovingForward.featureScale",
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOut),
            repeatMode = RepeatMode.Restart,
        ),
    )
    Face(modifier = modifier, featureScale = featureScale)
}

@Preview
@Composable
private fun FacePreview() {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val featureOffsetX by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        label = "featureOffsetX",
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Preview {
        Surface {
            Face(
                featureOffsetX = featureOffsetX,
                featureOffsetY = 0.5f,
                modifier = Modifier
                    .padding(2.dp)
                    .size(64.dp),
            )
        }
    }
}

@Composable
fun LottieFace(modifier: Modifier = Modifier, startFrame: Int = 0, endFrame: Int = 185) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.si_anim_face))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        clipSpec = LottieClipSpec.Frame(startFrame, endFrame),
        reverseOnRepeat = true,
        ignoreSystemAnimatorScale = true,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        modifier = modifier,
        composition = composition,
        progress = { progress },
    )
}

@Composable
fun LottieFaceLookingLeft(modifier: Modifier = Modifier) {
    LottieFace(modifier = modifier, startFrame = 0, endFrame = 30)
}

@Composable
fun LottieFaceLookingRight(modifier: Modifier = Modifier) {
    LottieFace(modifier = modifier, startFrame = 60, endFrame = 90)
}

@Composable
fun LottieFaceLookingUp(modifier: Modifier = Modifier) {
    LottieFace(modifier = modifier, startFrame = 120, endFrame = 149)
}

@Preview
@Composable
private fun LottieFacePreview() {
    Preview {
        Surface {
            LottieFaceLookingUp()
        }
    }
}
