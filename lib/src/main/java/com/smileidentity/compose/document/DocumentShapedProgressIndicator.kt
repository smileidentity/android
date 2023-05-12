package com.smileidentity.compose.document

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreview
import com.smileidentity.compose.shape.DocumentShape

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
        drawRect(color = backgroundColor)

        // 4. Draw the Progress Indicator Track
        drawPath(DocumentShape.path, color = incompleteProgressStrokeColor, style = stroke)
    }
}

@SmilePreview
@Composable
private fun DocumentShapedProgressIndicatorPreview() {
    Preview {
        DocumentShapedProgressIndicator(documentFillPercent = 0.25f)
    }
}
