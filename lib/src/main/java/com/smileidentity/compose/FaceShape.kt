package com.smileidentity.compose

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class FaceShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        return Outline.Generic(path)
    }

    companion object {
        /**
         * Derived from the si_face_outline.xml vector drawable. It had to be manually converted to
         * a Path object as Canvas doesn't support the "inverse" operation we need to perform to
         * cut the face shape out of the translucent background. If the vector needs to be updated,
         * this path can be regenerated at: https://codecrafted.net/svgtoandroid
         */
        val path = Path().apply {
            reset()
            moveTo(4.2f, 125.3f)
            cubicTo(4.8f, 115.9f, 6.2f, 92.0f, 17.4f, 69.9f)
            cubicTo(37.1f, 30.7f, 77.2f, 4.0f, 123.1f, 4.0f)
            cubicTo(189.0f, 4.0f, 241.9f, 58.1f, 241.9f, 125.3f)
            cubicTo(241.9f, 141.3f, 237.2f, 175.2f, 235.0f, 188.8f)
            cubicTo(232.5f, 203.67f, 229.2f, 218.39f, 225.1f, 232.9f)
            cubicTo(206.5f, 293.9f, 162.9f, 336.8f, 123.1f, 336.8f)
            cubicTo(69.6f, 336.8f, 31.9f, 267.3f, 21.0f, 233.5f)
            cubicTo(15.9f, 217.5f, 11.3f, 192.2f, 10.5f, 188.3f)
            cubicTo(6.2f, 164.5f, 2.9f, 146.9f, 4.2f, 125.3f)
        }
    }
}
