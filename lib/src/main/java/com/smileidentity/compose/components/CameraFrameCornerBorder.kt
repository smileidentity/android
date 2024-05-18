package com.smileidentity.compose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws only the corners of a rounded rectangle.
 */
@Composable
fun CameraFrameCornerBorder(
    cornerRadius: Dp,
    strokeWidth: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    extendCornerBy: Dp = cornerRadius,
) {
    Canvas(modifier) {
        cameraFrameCornerBorder(
            cornerRadius = cornerRadius.toPx(),
            strokeWidth = strokeWidth.toPx(),
            color = color,
            extendCornerBy = extendCornerBy.toPx(),
        )
    }
}

/**
 * Draws only the corners of a rounded rectangle.
 */
fun DrawScope.cameraFrameCornerBorder(
    cornerRadius: Float,
    strokeWidth: Float,
    color: Color,
    extendCornerBy: Float = cornerRadius,
) {
    val radius = CornerRadius(cornerRadius)
    val roundedRect = RoundRect(
        rect = size.toRect(),
        cornerRadius = radius,
    )
    drawPath(
        path = Path().apply { addRoundRect(roundedRect) },
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Butt,
            pathEffect = roundedRectCornerDashPathEffect(
                cornerRadius = radius.x,
                roundedRectSize = size,
                extendCornerBy = extendCornerBy,
            ),
        ),
    )
}

@Preview
@Composable
private fun CameraFrameCornerBorderPreview() {
    CameraFrameCornerBorder(
        cornerRadius = 16.dp,
        strokeWidth = 4.dp,
        color = Color.Green,
        modifier = Modifier
            .size(200.dp),
    )
}
