package com.smileidentity.compose.document

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreview

const val DEFAULT_DOCUMENT_ASPECT_RATIO = 3.56f
const val DOCUMENT_BOUNDING_BOX_MARGINS = 30f
val DOCUMENT_BOUNDING_BOX_OFFSET = 150.dp
val DOCUMENT_BOUNDING_BOX_RADIUS = CornerRadius(30f, 30f)

/**
 * A simple bounding box that takes the shape of a document, has a semi-transparent background, and
 * has an outline which changes color depending on edge detection algorithm
 *
 * @param modifier The modifier to be applied to the indicator.
 * @param aspectRatio The aspect ratio of the document, used to calculate the height of the view.
 * @param strokeWidth The width of the progress indicator stroke.
 * @param areEdgesDetected A boolean flag that is updated when document edges are within bounding box edges
 * @param backgroundColor The color of the background that is drawn around the document shape.
 */
@Composable
fun DocumentShapedBoundingBox(
    modifier: Modifier = Modifier,
    aspectRatio: Float?,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    areEdgesDetected: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.scrim,
) {
    val strokeColor = if (areEdgesDetected) MaterialTheme.colorScheme.tertiary else Color.Gray

    Canvas(
        modifier
            .fillMaxSize(),
    ) {
        // 1. Set the background color using a rectangle
        drawRect(backgroundColor)

        // 2. Draw the outline of the bounding box and add a stroke that shows different edge detection states
        val outlineBoundingBoxWidth = size.width - DOCUMENT_BOUNDING_BOX_MARGINS
        val outlineBoundingBoxHeight = outlineBoundingBoxWidth / (aspectRatio ?: DEFAULT_DOCUMENT_ASPECT_RATIO)
        drawRoundRect(
            color = strokeColor,
            topLeft = Offset(
                x = (size.width - outlineBoundingBoxWidth) / 2,
                y = DOCUMENT_BOUNDING_BOX_OFFSET.toPx(),
            ),
            size = Size(width = outlineBoundingBoxWidth, height = outlineBoundingBoxHeight),
            cornerRadius = DOCUMENT_BOUNDING_BOX_RADIUS,
            style = Stroke(width = strokeWidth.toPx()),
        )

        // 3. Draw the transparent bounding box, and make it slightly smaller than the outline box
        val boundingBoxWidth = size.width - DOCUMENT_BOUNDING_BOX_MARGINS - strokeWidth.toPx()
        drawRoundRect(
            color = Color.Transparent,
            blendMode = BlendMode.Clear,
            topLeft = Offset(
                x = (size.width - boundingBoxWidth) / 2,
                y = DOCUMENT_BOUNDING_BOX_OFFSET.toPx() + (strokeWidth.toPx() / 2),
            ),
            size = Size(
                width = (outlineBoundingBoxWidth - strokeWidth.toPx()),
                height = (outlineBoundingBoxHeight - strokeWidth.toPx()),
            ),
            cornerRadius = DOCUMENT_BOUNDING_BOX_RADIUS,
        )
    }
}

@SmilePreview
@Composable
private fun DocumentShapedProgressIndicatorPreview() {
    Preview {
        DocumentShapedBoundingBox(aspectRatio = 16f / 9f)
    }
}
