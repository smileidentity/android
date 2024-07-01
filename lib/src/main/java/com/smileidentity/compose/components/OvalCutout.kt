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
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import com.smileidentity.compose.preview.Preview

@Composable
fun OvalCutout(
    @FloatRange(from = 0.0, to = 1.0) faceFillPercent: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.scrim,
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
        val ovalPath = Path().apply {
            addOval(
                Rect(
                    size = constrainedSize,
                    offset = Offset(
                        x = (size.width - constrainedSize.width) / 2,
                        y = (size.height - constrainedSize.height) / 2,
                    ),
                ),
            )
        }
        clipPath(ovalPath, clipOp = ClipOp.Difference) {
            drawRect(color = backgroundColor)
        }
    }
}

@Composable
@Preview
private fun OvalCutoutPreview() {
    Preview {
        OvalCutout(faceFillPercent = 0.25f)
    }
}
