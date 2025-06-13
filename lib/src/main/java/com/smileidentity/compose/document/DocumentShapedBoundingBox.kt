package com.smileidentity.compose.document

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
            // .fillMaxSize(), todo undo back to this
            .fillMaxWidth()
            .height(300.dp),
    ) {
        // Define the outline of the bounding box and add a stroke that shows different edge
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

        // Create the transparent bounding box, and make it slightly smaller than the outline box
        val roundRectPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(
                            x = outlineBoundingBoxX + (strokeWidth.toPx() / 2),
                            y = outlineBoundingBoxY + (strokeWidth.toPx() / 2),
                        ),
                        size = Size(
                            width = (outlineBoundingBoxWidth - strokeWidth.toPx()),
                            height = (outlineBoundingBoxHeight - strokeWidth.toPx()),
                        ),
                    ),
                    cornerRadius = DOCUMENT_BOUNDING_BOX_RADIUS - CornerRadius(
                        strokeWidth.value,
                        strokeWidth.value,
                    ),
                ),
            )
        }

        // Draw the bounding box and clip the background to the shape of the document
        clipPath(roundRectPath, clipOp = ClipOp.Difference) {
            // Draw the background
            drawRect(color = backgroundColor)

            // Draw the outline
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
        }
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
