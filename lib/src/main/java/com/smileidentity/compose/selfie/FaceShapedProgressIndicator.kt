package com.smileidentity.compose.selfie

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import kotlin.math.sqrt

/**
 * A progress indicator that is shaped like a face, has a semi-transparent background, and where the
 * progress fills up symmetrically on either side of the face.
 *
 * @param progress The progress of the indicator, from 0 to 1.
 * @param faceFillPercent The percent of the Canvas that should be filled by the face shape
 * @param modifier The modifier to be applied to the indicator.
 * @param strokeWidth The width of the progress indicator stroke.
 * @param completeProgressStrokeColor The color of the progress indicator stroke.
 * @param incompleteProgressStrokeColor The color of the progress indicator track.
 * @param backgroundColor The color of the background that is drawn around the face shape.
 */
@Composable
fun FaceShapedProgressIndicator(
    progress: Float,
    @FloatRange(from = 0.0, to = 1.0) faceFillPercent: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp,
    completeProgressStrokeColor: Color = MaterialTheme.colorScheme.tertiary,
    incompleteProgressStrokeColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
    backgroundColor: Color = MaterialTheme.colorScheme.scrim,
) {
    val stroke = with(LocalDensity.current) { Stroke(width = strokeWidth.toPx()) }
    Canvas(modifier = modifier.progressSemantics(progress).fillMaxSize()) {
        val faceShapeBounds = FaceShape.path.getBounds()
        // Scale the face shape to the desired size
        val faceArea = faceShapeBounds.width * faceShapeBounds.height
        val canvasArea = size.width * size.height
        val scaleFactor = sqrt(faceFillPercent * canvasArea / faceArea)
        // 1. Move the Face Shape to the center of the Canvas
        val centeredFaceOffset = with((size.center - faceShapeBounds.center)) {
            // 1.5. Offset a little bit above center
            copy(y = y - faceShapeBounds.height / (5 * scaleFactor))
        }
        FaceShape.path.translate(centeredFaceOffset)
        scale(scaleFactor) {
            // 2. Draw the Face Shape, clipping it out of the background
            clipPath(FaceShape.path, clipOp = ClipOp.Difference) {
                // 3. Set the background color using a rectangle
                drawRect(color = backgroundColor)
            }

            // No need to draw the progress if the stroke width is 0
            if (strokeWidth == 0.dp) return@Canvas

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

@SmilePreviews
@Composable
private fun FaceShapedProgressIndicatorPreview() {
    Preview {
        FaceShapedProgressIndicator(progress = 0.5f, faceFillPercent = 0.25f)
    }
}
