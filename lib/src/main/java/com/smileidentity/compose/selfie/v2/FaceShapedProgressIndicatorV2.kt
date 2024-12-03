package com.smileidentity.compose.selfie.v2

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.compose.selfie.FaceShape
import kotlin.math.sqrt

@Composable
fun FaceShapedProgressIndicatorV2(
    progress: Float,
    @FloatRange(from = 0.0, to = 1.0) faceFillPercent: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp,
    showLeftCutout: Boolean = false,
    showRightCutout: Boolean = false,
    showTopCutout: Boolean = false,
    completeProgressStrokeColor: Color = MaterialTheme.colorScheme.tertiary,
    incompleteProgressStrokeColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
    backgroundColor: Color = MaterialTheme.colorScheme.scrim,
) {
    val stroke = with(LocalDensity.current) { Stroke(width = strokeWidth.toPx()) }
    Canvas(
        modifier = modifier
            .progressSemantics(progress)
            .fillMaxSize(),
    ) {
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
        FaceShape.path.translate(offset = centeredFaceOffset)

        scale(scale = scaleFactor) {
            // 2. Draw the Face Shape and cutouts, clipping them out of the background
            val combinedPath = Path().apply {
                addPath(path = FaceShape.path)

                val sideOffset = faceShapeBounds.width * 0.05f

                // Add left cutout
                if (showLeftCutout) {
                    val leftX = faceShapeBounds.left - sideOffset
                    val yStartLeft = faceShapeBounds.top + faceShapeBounds.height * 0.3f
                    val yEndLeft = faceShapeBounds.bottom - faceShapeBounds.height * 0.3f
                    moveTo(x = leftX, y = yStartLeft)
                    cubicTo(
                        x1 = leftX - sideOffset * 2f,
                        y1 = yStartLeft + faceShapeBounds.height * 0.15f,
                        x2 = leftX - sideOffset * 2f,
                        y2 = yEndLeft - faceShapeBounds.height * 0.15f,
                        x3 = leftX,
                        y3 = yEndLeft,
                    )
                }

                // Add right cutout
                if (showRightCutout) {
                    val rightX = faceShapeBounds.right + sideOffset
                    val yStartRight = faceShapeBounds.top + faceShapeBounds.height * 0.3f
                    val yEndRight = faceShapeBounds.bottom - faceShapeBounds.height * 0.3f
                    moveTo(x = rightX, y = yStartRight)
                    cubicTo(
                        x1 = rightX + sideOffset * 2f,
                        y1 = yStartRight + faceShapeBounds.height * 0.15f,
                        x2 = rightX + sideOffset * 2f,
                        y2 = yEndRight - faceShapeBounds.height * 0.15f,
                        x3 = rightX,
                        y3 = yEndRight,
                    )
                }

                // Add top cutout
                if (showTopCutout) {
                    val topY = faceShapeBounds.top - faceShapeBounds.height * 0.05f
                    val topWidth = faceShapeBounds.width * 0.4f
                    moveTo(
                        x = faceShapeBounds.center.x - topWidth / 2,
                        y = topY,
                    )
                    quadraticTo(
                        x1 = faceShapeBounds.center.x,
                        y1 = topY - faceShapeBounds.height * 0.02f,
                        x2 = faceShapeBounds.center.x + topWidth / 2,
                        y2 = topY,
                    )
                }
            }

            // Draw background with all cutouts
            clipPath(path = combinedPath, clipOp = ClipOp.Difference) {
                drawRect(color = backgroundColor)
            }

            // No need to draw the progress if the stroke width is 0
            if (strokeWidth == 0.dp) return@Canvas

            // Draw the complete path for the progress indicator track
            drawPath(path = combinedPath, color = incompleteProgressStrokeColor, style = stroke)

            // To prevent a bug where the progress initially shows up as a full circle
            if (progress == 0f) return@Canvas

            // Note: Height grows downwards
            val faceShapeSize = faceShapeBounds.size
            val left = faceShapeBounds.left - strokeWidth.toPx() / 2
            val top = faceShapeBounds.top - strokeWidth.toPx() / 2
            val right = left + faceShapeSize.width + strokeWidth.toPx()
            val bottom = top + (faceShapeSize.height + strokeWidth.toPx()) * (1 - progress)

            // Draw the progress indicator by clipping the combined path
            clipRect(
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                clipOp = ClipOp.Difference,
            ) {
                drawPath(path = combinedPath, color = completeProgressStrokeColor, style = stroke)
            }
        }
    }
}

@SmilePreviews
@Composable
private fun FaceShapedProgressIndicatorV2Preview() {
    Preview {
        FaceShapedProgressIndicatorV2(progress = 0f, faceFillPercent = 0.25f)
    }
}
