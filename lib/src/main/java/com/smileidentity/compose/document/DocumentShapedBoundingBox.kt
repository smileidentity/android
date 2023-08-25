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
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews

const val DOCUMENT_BOUNDING_BOX_MARGINS = 64f
val DOCUMENT_BOUNDING_BOX_RADIUS = CornerRadius(30f, 30f)

/**
 * A simple bounding box that takes the shape of a document, has a semi-transparent background, and
 * has an outline which changes color depending on edge detection algorithm
 *
 * @param aspectRatio The aspect ratio of the document, used to calculate the height of the view.
 * @param areEdgesDetected A boolean flag that is updated when document edges are within bounding
 * box edges
 * @param modifier The modifier to be applied to the indicator.
 * @param strokeWidth The width of the progress indicator stroke.
 * @param backgroundColor The color of the background that is drawn around the document shape.
 */
@Composable
fun DocumentShapedBoundingBox(
    aspectRatio: Float,
    areEdgesDetected: Boolean,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    backgroundColor: Color = MaterialTheme.colorScheme.scrim,
) {
    val strokeColor = if (areEdgesDetected) MaterialTheme.colorScheme.tertiary else Color.Gray

    Canvas(
        modifier = modifier
            .fillMaxSize(),
    ) {
        // 1. Set the background color using a rectangle
        drawRect(backgroundColor)

        // 2. Draw the outline of the bounding box and add a stroke that shows different edge
        // detection states

        val (outlineBoundingBoxWidth, outlineBoundingBoxHeight) = if (aspectRatio >= 1) {
            // Horizontal ID
            val outlineBoundingBoxWidth = size.width - DOCUMENT_BOUNDING_BOX_MARGINS
            val outlineBoundingBoxHeight = outlineBoundingBoxWidth / aspectRatio
            outlineBoundingBoxWidth to outlineBoundingBoxHeight
        } else {
            // Vertical ID
            val outlineBoundingBoxHeight = size.height - DOCUMENT_BOUNDING_BOX_MARGINS
            val outlineBoundingBoxWidth = outlineBoundingBoxHeight * aspectRatio
            outlineBoundingBoxWidth to outlineBoundingBoxHeight
        }
        val outlineBoundingBoxX = (size.width - outlineBoundingBoxWidth) / 2
        val outlineBoundingBoxY = (size.height - outlineBoundingBoxHeight) / 2
        drawRoundRect(
            color = strokeColor,
            topLeft = Offset(
                x = outlineBoundingBoxX,
                y = outlineBoundingBoxY,
            ),
            size = Size(width = outlineBoundingBoxWidth, height = outlineBoundingBoxHeight),
            cornerRadius = DOCUMENT_BOUNDING_BOX_RADIUS,
            style = Stroke(width = strokeWidth.toPx()),
        )

        // 3. Draw the transparent bounding box, and make it slightly smaller than the outline box
        drawRoundRect(
            color = Color.Transparent,
            blendMode = BlendMode.Clear,
            topLeft = Offset(
                x = outlineBoundingBoxX + (strokeWidth.toPx() / 2),
                y = outlineBoundingBoxY + (strokeWidth.toPx() / 2),
            ),
            size = Size(
                width = (outlineBoundingBoxWidth - strokeWidth.toPx()),
                height = (outlineBoundingBoxHeight - strokeWidth.toPx()),
            ),
            cornerRadius = DOCUMENT_BOUNDING_BOX_RADIUS - CornerRadius(
                strokeWidth.value,
                strokeWidth.value,
            ),
        )
    }
}

@SmilePreviews
@Composable
private fun DocumentShapedBoundingBoxPreview() {
    Preview {
        DocumentShapedBoundingBox(
            areEdgesDetected = true,
            aspectRatio = 16f / 9f,
        )
    }
}
