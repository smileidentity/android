package com.smileidentity.compose.components

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smileidentity.compose.preview.Preview

@Composable
fun OvalCutout(
    topProgress: Float,
    rightProgress: Float,
    leftProgress: Float,
    @FloatRange(from = 0.0, to = 1.0) faceFillPercent: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.scrim,
    arcBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    arcColor: Color = MaterialTheme.colorScheme.tertiary,
) {
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

        drawPath(
            path = ovalPath,
            color = arcBackgroundColor,
            style = Stroke(width = strokeWidth.toPx()),
        )

        clipPath(ovalPath, clipOp = ClipOp.Difference) {
            drawRect(color = backgroundColor)
        }

        val arcWidth = constrainedSize.width * 0.55f
        val arcHeight = constrainedSize.height * 0.55f

        // Calculate arc center point
        val arcCenter = Offset(
            x = ovalOffset.x + constrainedSize.width / 2,
            y = ovalOffset.y + constrainedSize.height / 2,
        )

        val arcSize = Size(width = arcWidth * 2, height = arcHeight * 2)
        val arcStroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val arcTopLeft = Offset(
            x = arcCenter.x - arcWidth,
            y = arcCenter.y - arcHeight,
        )

        when {
            // top arc
            topProgress > 0 -> {
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
            // right arc
            rightProgress > 0 -> {
                drawArc(
                    color = arcBackgroundColor,
                    startAngle = 330f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = arcStroke,
                )
                drawArc(
                    color = arcColor,
                    startAngle = 330f,
                    sweepAngle = 60f * rightProgress,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = arcStroke,
                )
            }
            // left arc
            leftProgress > 0 -> {
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
        }
    }
}

@Composable
@Preview
private fun OvalCutoutPreview() {
    Preview {
        OvalCutout(
            topProgress = 0.8f,
            rightProgress = 0.5f,
            leftProgress = 0.3f,
            faceFillPercent = 0.25f,
        )
    }
}
