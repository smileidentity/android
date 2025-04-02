package com.smileidentity.util.watermark.utils

import android.graphics.Color

object BitmapUtils {
    /**
     * Bitmap to Pixels then converting it to an ARGB int array.
     */
    @JvmStatic
    fun pixel2ARGBArray(inputPixels: IntArray): IntArray {
        val bitmapArray = IntArray(4 * inputPixels.size)
        for (i in inputPixels.indices) {
            bitmapArray[4 * i] = Color.alpha(inputPixels[i])
            bitmapArray[4 * i + 1] = Color.red(inputPixels[i])
            bitmapArray[4 * i + 2] = Color.green(inputPixels[i])
            bitmapArray[4 * i + 3] = Color.blue(inputPixels[i])
        }

        return bitmapArray
    }
}
