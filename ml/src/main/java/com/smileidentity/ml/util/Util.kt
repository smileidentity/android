package com.smileidentity.ml.util

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Size
import kotlin.math.min
import kotlin.math.roundToInt

// Check if the bounds of `boundingBox` fit within the limits of `cameraFrameBitmap`
internal fun validateRect(cameraFrameBitmap: Bitmap, boundingBox: Rect): Boolean =
    boundingBox.left >= 0 &&
        boundingBox.top >= 0 &&
        (boundingBox.left + boundingBox.width()) < cameraFrameBitmap.width &&
        (boundingBox.top + boundingBox.height()) < cameraFrameBitmap.height

/**
 * Get the size of a [Bitmap].
 */
internal fun Bitmap.size() = Size(this.width, this.height)

/**
 * Converts a size to rectangle with the top left corner at 0,0
 */
internal fun Size.toRect() = Rect(0, 0, this.width, this.height)

internal fun Bitmap.cropCenter(size: Size): Bitmap =
    if (size.width > width || size.height > height) {
        val cropRegion = size.scaleAndCenterWithin(Size(width, height))
        crop(cropRegion).scale(size)
    } else {
        crop(size.centerOn(size().toRect()))
    }

internal fun Bitmap.scale(size: Size, filter: Boolean = false): Bitmap =
    if (size.width == width && size.height == height) {
        this
    } else {
        Bitmap.createScaledBitmap(this, size.width, size.height, filter)
    }

/**
 * Center a size on a given rectangle. The size may be larger or smaller than the rect.
 */
internal fun Size.centerOn(rect: Rect) = Rect(
    /* left */
    rect.centerX() - this.width / 2,
    /* top */
    rect.centerY() - this.height / 2,
    /* right */
    rect.centerX() + this.width / 2,
    /* bottom */
    rect.centerY() + this.height / 2,
)

/**
 * Crop a [Bitmap] to a given [Rect]. The crop must have a positive area and must be contained
 * within the bounds of the source [Bitmap].
 */
internal fun Bitmap.crop(crop: Rect): Bitmap {
    require(crop.left < crop.right && crop.top < crop.bottom) { "Cannot use negative crop" }
    require(
        crop.left >= 0 &&
            crop.top >= 0 &&
            crop.bottom <= this.height &&
            crop.right <= this.width,
    ) {
        "Crop is larger than source image"
    }
    return Bitmap.createBitmap(this, crop.left, crop.top, crop.width(), crop.height())
}

/**
 * Scale up a [Size] so that it fills a [containingSize] while maintaining its original aspect
 * ratio.
 *
 * If using this to project a preview image onto a full camera image, This makes a few assumptions:
 * 1. the preview image [Size] and full image [containingSize] are centered relative to each other
 * 2. the preview image and the full image have the same orientation
 * 3. the preview image and the full image share either a horizontal or vertical field of view
 * 4. the non-shared field of view must be smaller on the preview image than the full image
 *
 * Note that the [Size] and the [containingSize] are allowed to have completely independent
 * resolutions.
 */
internal fun Size.scaleAndCenterWithin(containingSize: Size): Rect {
    val aspectRatio = width.toFloat() / height

    // Since the preview image may be at a different resolution than the full image, scale the
    // preview image to be circumscribed by the fullImage.
    val scaledSize = maxAspectRatioInSize(containingSize, aspectRatio)
    val left = (containingSize.width - scaledSize.width) / 2
    val top = (containingSize.height - scaledSize.height) / 2
    return Rect(
        left,
        top,
        left + scaledSize.width,
        top + scaledSize.height,
    )
}

/**
 * Determine the maximum size of rectangle with a given aspect ratio (X/Y) that can fit inside the
 * specified area.
 *
 * For example, if the aspect ratio is 1/2 and the area is 2x2, the resulting rectangle would be
 * size 1x2 and look like this:
 * ```
 *  ________
 * | |    | |
 * | |    | |
 * | |    | |
 * |_|____|_|
 * ```
 */
internal fun maxAspectRatioInSize(area: Size, aspectRatio: Float): Size {
    var width = area.width
    var height = (width / aspectRatio).roundToInt()

    return if (height <= area.height) {
        Size(area.width, height)
    } else {
        height = area.height
        width = (height * aspectRatio).roundToInt()
        Size(min(width, area.width), height)
    }
}
