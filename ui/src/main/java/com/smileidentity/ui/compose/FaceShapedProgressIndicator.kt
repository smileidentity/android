package com.smileidentity.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smileidentity.ui.theme.SmileIdentitySemiTransparentBackground

/**
 * A progress indicator that is shaped like a face, has a semi-transparent background, and where the
 * progress fills up symmetrically on either side of the face.
 *
 * @param progress The progress of the indicator, from 0 to 1.
 * @param faceHeight The height of the face shape. The aspect ratio of the face shape is preserved.
 * @param modifier The modifier to be applied to the indicator.
 * @param strokeWidth The width of the progress indicator stroke.
 * @param completeProgressStrokeColor The color of the progress indicator stroke.
 * @param incompleteProgressStrokeColor The color of the progress indicator track.
 * @param backgroundColor The color of the background that is drawn around the face shape.
 */
@Composable
fun FaceShapedProgressIndicator(
    progress: Float,
    faceHeight: Dp,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    completeProgressStrokeColor: Color = ProgressIndicatorDefaults.circularColor,
    incompleteProgressStrokeColor: Color = Color.Gray,
    backgroundColor: Color = SmileIdentitySemiTransparentBackground,
) {
    val stroke = with(LocalDensity.current) { Stroke(strokeWidth.toPx()) }
    Canvas(modifier.fillMaxSize()) {
        val faceShapeBounds = FaceShape.path.getBounds()
        // Scale the face shape to the desired size
        scale(faceHeight.toPx() / faceShapeBounds.height) {
            // 1. Move the Face Shape to the center of the Canvas
            val centeredFaceOffset = with((size.center - faceShapeBounds.center)) {
                // 1.5. Offset a little bit above center
                copy(y = y - faceShapeBounds.height / 5)
            }
            FaceShape.path.translate(centeredFaceOffset)
            // 2. Draw the Face Shape, clipping it out of the background
            clipPath(FaceShape.path, clipOp = ClipOp.Difference) {
                // 3. Set the background color using a rectangle
                drawRect(color = backgroundColor)
            }

            // 4. Draw the Progress Indicator Track
            drawPath(FaceShape.path, color = incompleteProgressStrokeColor, style = stroke)

            // To prevent a bug where the progress initially shows up as a full circle
            if (progress == 0f) return@Canvas

            // Note: Height grows downwards
            val faceShapeSize = faceShapeBounds.size
            val left = faceShapeBounds.left - strokeWidth.toPx() / 2
            val top = faceShapeBounds.top - strokeWidth.toPx() / 2
            val right = left + faceShapeSize.width + strokeWidth.toPx()
            val bottom = top + (faceShapeSize.height + strokeWidth.toPx()) * (1 - progress)
            // 5. Draw the Progress Indicator by clipping the face shape path out of a rectangle
            clipRect(left, top, right, bottom, clipOp = ClipOp.Difference) {
                drawPath(FaceShape.path, color = completeProgressStrokeColor, style = stroke)
            }
        }
    }
}

@Preview
@Composable
private fun FaceShapedProgressIndicatorPreview() {
    FaceShapedProgressIndicator(progress = 0.5f, faceHeight = 400.dp)
}
