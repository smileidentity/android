package com.smileidentity.compose.selfie

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter

object FaceShapeV2 {
    private val p = Paint()
    private val ps = Paint()
    private val t = Path()
    private val m = Matrix()
    private var od = 0f
    internal var cf: ColorFilter? = null

    /**
     * IMPORTANT: Due to the static usage of this class this
     * method sets the tint color statically. So it is highly
     * recommended to call the clearColorTint method when you
     * have finished drawing.
     *
     * Sets the color to use when drawing the SVG. This replaces
     * all parts of the drawable which are not completely
     * transparent with this color.
     */
    fun setColorTint(color: Int) {
        cf = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    fun clearColorTint(color: Int) {
        cf = null
    }

    @JvmOverloads
    fun draw(
        c: Canvas,
        w: Int,
        h: Int,
        dx: Int = 0,
        dy: Int = 0,
    ) {
        val ow = 297f
        val oh = 256f

        od = if ((w / ow < h / oh)) w / ow else h / oh

        r()
        c.save()
        c.translate((w - od * ow) / 2f + dx, (h - od * oh) / 2f + dy)

        m.reset()
        m.setScale(od, od)

        c.save()
        p.color = Color.argb(0, 0, 0, 0)
        ps.color = Color.argb(0, 0, 0, 0)
        ps.strokeCap = Paint.Cap.BUTT
        ps.strokeJoin = Paint.Join.MITER
        ps.strokeMiter = 4.0f * od
        c.scale(1.0f, 1.0f)
        c.save()
        ps.color = Color.parseColor("#2CC05C")
        ps.strokeWidth = 12.0f * od
        ps.strokeCap = Paint.Cap.ROUND
        t.reset()
        t.moveTo(74.0f, 27.0f)
        t.cubicTo(121.34f, -4.15f, 185.18f, 2.34f, 223.0f, 27.0f)
        t.transform(m)
        c.drawPath(t, p)
        c.drawPath(t, ps)
        c.restore()
        r(5, 0, 2, 6, 3)
        ps.color = Color.parseColor("#2CC05C")
        ps.strokeWidth = 12.0f * od
        ps.strokeCap = Paint.Cap.ROUND
        c.save()
        t.reset()
        t.moveTo(19.0f, 249.0f)
        t.cubicTo(5.32f, 223.69f, 2.71f, 129.42f, 9.88f, 119.0f)
        t.transform(m)
        c.drawPath(t, p)
        c.drawPath(t, ps)
        c.restore()
        r(5, 0, 2, 6, 3, 7, 4, 1)
        c.save()
        t.reset()
        t.moveTo(278.0f, 250.0f)
        t.cubicTo(291.68f, 224.69f, 294.29f, 130.42f, 287.12f, 120.0f)
        t.transform(m)
        c.drawPath(t, p)
        c.drawPath(t, ps)
        c.restore()
        r(5, 0, 2, 6, 3, 7, 4, 1)
        c.restore()
        r()

        c.restore()
    }

    private fun r(vararg o: Int) {
        p.reset()
        ps.reset()
        if (cf != null) {
            p.setColorFilter(cf)
            ps.setColorFilter(cf)
        }
        p.isAntiAlias = true
        ps.isAntiAlias = true
        p.style = Paint.Style.FILL
        ps.style = Paint.Style.STROKE
        for (i in o) {
            when (i) {
                0 -> ps.color = Color.argb(0, 0, 0, 0)
                1 -> ps.strokeCap = Paint.Cap.ROUND
                2 -> ps.strokeCap = Paint.Cap.BUTT
                3 -> ps.strokeMiter = 4.0f * od
                4 -> ps.strokeWidth = 12.0f * od
                5 -> p.color = Color.argb(0, 0, 0, 0)
                6 -> ps.strokeJoin = Paint.Join.MITER
                7 -> ps.color = Color.parseColor("#2CC05C")
            }
        }
    }
}
