package com.smileidentity.ml.util

import android.graphics.Bitmap
import android.graphics.Rect

// Check if the bounds of `boundingBox` fit within the limits of `cameraFrameBitmap`
internal fun validateRect(cameraFrameBitmap: Bitmap, boundingBox: Rect): Boolean =
    boundingBox.left >= 0 &&
        boundingBox.top >= 0 &&
        (boundingBox.left + boundingBox.width()) < cameraFrameBitmap.width &&
        (boundingBox.top + boundingBox.height()) < cameraFrameBitmap.height
