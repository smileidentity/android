package com.smileidentity.compose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CameraFrameCornerBorder(
    cornerRadius: Dp,
    strokeWith: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    extendCornerBy: Dp = cornerRadius,
) {
    Canvas(modifier) {
        val radius = CornerRadius(cornerRadius.toPx())
        val roundedRect = RoundRect(
            rect = size.toRect(),
            cornerRadius = radius,
        )
        drawPath(
            path = Path().apply { addRoundRect(roundedRect) },
            color = color,
            style = Stroke(
                width = strokeWith.toPx(),
                cap = StrokeCap.Round,
                pathEffect = roundedRectCornerDashPathEffect(
                    cornerRadius = radius.x,
                    roundedRectSize = size,
                    extendCornerBy = extendCornerBy.toPx(),
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun CameraFrameCornerBorderPreview() {
    CameraFrameCornerBorder(
        cornerRadius = 16.dp,
        strokeWith = 4.dp,
        color = Color.Green,
        modifier = Modifier
            .padding(4.dp)
            .size(200.dp),
    )
}
