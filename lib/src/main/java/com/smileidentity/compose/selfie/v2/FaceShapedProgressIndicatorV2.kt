package com.smileidentity.compose.selfie.v2

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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
    indicatorWidth: Dp = 2.dp,
    indicatorColor: Color = Color(0xFF11B33E),
    backgroundColor: Color = Color.DarkGray,
    cutoutColor: Color = Color.LightGray,
) {
    val stroke = with(LocalDensity.current) { Stroke(width = indicatorWidth.toPx()) }
    Canvas(
        modifier = modifier
            .progressSemantics(progress)
            .fillMaxSize(),
    ) {
        val faceShapeBounds = FaceShape.path.getBounds()
        val faceArea = faceShapeBounds.width * faceShapeBounds.height
        val canvasArea = size.width * size.height
        val scaleFactor = sqrt(faceFillPercent * canvasArea / faceArea)

        val centeredFaceOffset = with((size.center - faceShapeBounds.center)) {
            copy(y = y - faceShapeBounds.height / (5 * scaleFactor))
        }
        FaceShape.path.translate(centeredFaceOffset)

        scale(scaleFactor) {
            clipPath(FaceShape.path, clipOp = ClipOp.Difference) {
                drawRect(color = backgroundColor)
            }
            drawPath(FaceShape.path, color = cutoutColor)

            val sideOffset = faceShapeBounds.width * 0.05f

            // Left indicator with pronounced outward curve
            val leftX = faceShapeBounds.left - sideOffset
            drawPath(
                path = Path().apply {
                    val yStart = faceShapeBounds.top + faceShapeBounds.height * 0.3f
                    val yEnd = faceShapeBounds.bottom - faceShapeBounds.height * 0.3f
                    moveTo(leftX, yStart)
                    cubicTo(
                        x1 = leftX - sideOffset * 2f,
                        y1 = yStart + faceShapeBounds.height * 0.15f,
                        x2 = leftX - sideOffset * 2f,
                        y2 = yEnd - faceShapeBounds.height * 0.15f,
                        x3 = leftX,
                        y3 = yEnd,
                    )
                },
                color = indicatorColor,
                style = stroke,
            )

            // Right indicator with pronounced outward curve
            val rightX = faceShapeBounds.right + sideOffset
            drawPath(
                path = Path().apply {
                    val yStart = faceShapeBounds.top + faceShapeBounds.height * 0.3f
                    val yEnd = faceShapeBounds.bottom - faceShapeBounds.height * 0.3f
                    moveTo(rightX, yStart)
                    cubicTo(
                        x1 = rightX + sideOffset * 2f,
                        y1 = yStart + faceShapeBounds.height * 0.15f,
                        x2 = rightX + sideOffset * 2f,
                        y2 = yEnd - faceShapeBounds.height * 0.15f,
                        x3 = rightX,
                        y3 = yEnd,
                    )
                },
                color = indicatorColor,
                style = stroke,
            )

            // Top indicator
            val topY = faceShapeBounds.top - faceShapeBounds.height * 0.05f
            val topWidth = faceShapeBounds.width * 0.4f
            drawPath(
                path = Path().apply {
                    moveTo(
                        faceShapeBounds.center.x - topWidth / 2,
                        topY,
                    )
                    quadraticBezierTo(
                        faceShapeBounds.center.x,
                        topY - faceShapeBounds.height * 0.02f,
                        faceShapeBounds.center.x + topWidth / 2,
                        topY,
                    )
                },
                color = indicatorColor,
                style = stroke,
            )
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
