package com.smileidentity.compose.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect

/**
 * Returns a [PathEffect] that draws a dashed line around the corners of a rounded rectangle
 *
 * @param cornerRadius The radius of the rounded corners
 * @param roundedRectSize The size of the rounded rectangle
 * @param extendCornerBy The amount to extend the corner dashes by. This will be distributed evenly
 * on both ends of each corner
 */
internal fun roundedRectCornerDashPathEffect(
    cornerRadius: Float,
    roundedRectSize: Size,
    extendCornerBy: Float = 0f,
): PathEffect {
    // Each corner's length is a quarter circle
    val cornerLength = (2 * Math.PI * cornerRadius / 4f).toFloat() + extendCornerBy

    // There are 2 corners, so we subtract 2 * radius from the width (same goes for height)
    val cornerHeight = cornerRadius + (extendCornerBy / 2)
    val roundedRectWidthExcludingCorners = roundedRectSize.width - (2 * cornerHeight)
    val roundedRectHeightExcludingCorners = roundedRectSize.height - (2 * cornerHeight)

    return PathEffect.dashPathEffect(
        intervals = floatArrayOf(
            cornerLength,
            roundedRectHeightExcludingCorners,
            cornerLength,
            roundedRectWidthExcludingCorners,
            cornerLength,
            roundedRectHeightExcludingCorners,
            cornerLength,
            roundedRectWidthExcludingCorners,
        ),
        phase = cornerLength - (extendCornerBy / 2),
    )
}
