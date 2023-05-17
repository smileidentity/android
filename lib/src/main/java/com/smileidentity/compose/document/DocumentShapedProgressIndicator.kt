package com.smileidentity.compose.document

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreview
import kotlin.math.sqrt

@Composable
fun DocumentShapedProgressIndicator(
    @FloatRange(from = 0.0, to = 1.0) documentFillPercent: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    completeProgressStrokeColor: Color = MaterialTheme.colorScheme.tertiary,
    incompleteProgressStrokeColor: Color = Color.Gray,
    backgroundColor: Color = MaterialTheme.colorScheme.scrim,
) {
    val stroke = with(LocalDensity.current) { Stroke(strokeWidth.toPx()) }
    Canvas(
        modifier
            .fillMaxSize(),
    ) {
        val rectWidth = size.width * 0.8F
        val rectHeight = size.height / 4
        val cornerRadius = 20f // corner radius

        val rect = Rect(0F, 0F, rectWidth, rectHeight)
        val path = createRectPath(rect)

        val documentShapeBounds = path.getBounds()

        // Scale the document shape to the desired size
        val documentArea = documentShapeBounds.width * documentShapeBounds.height

        val canvasArea = size.width * size.height
        val scaleFactor = sqrt(documentFillPercent * canvasArea / documentArea)
        // 1. Move the Document Shape to the center of the Canvas
        val centeredDocumentOffset = with((size.center - documentShapeBounds.center)) {
            // 1.5. Offset a little bit above center
            copy(y = y - documentShapeBounds.height / (5 * scaleFactor))
        }
        path.translate(centeredDocumentOffset)

        scale(scaleFactor) {
            // 2. Draw the Face Shape, clipping it out of the background
            clipPath(path, clipOp = ClipOp.Difference) {
                // 3. Set the background color using a rectangle
                drawRect(color = backgroundColor)
            }

            // 4. Draw the Progress Indicator Track
            drawPath(path, color = incompleteProgressStrokeColor, style = stroke)
        }
    }
}

fun createRectPath(rect: Rect): Path {
    val path = Path()
    path.moveTo(rect.left, rect.top)
    path.lineTo(rect.right, rect.top)
    path.lineTo(rect.right, rect.bottom)
    path.lineTo(rect.left, rect.bottom)
    path.close()
    return path
}

@SmilePreview
@Composable
private fun DocumentShapedProgressIndicatorPreview() {
    Preview {
        DocumentShapedProgressIndicator(documentFillPercent = 0.25f)
    }
}
