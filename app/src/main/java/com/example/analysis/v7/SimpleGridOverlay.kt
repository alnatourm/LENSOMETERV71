package com.example.analysis.v7

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/** Reference dots: green. Lens dots: red. Arrows: yellow. */
object SimpleGridOverlay {
    fun render(background: Bitmap, result: SimpleGridMatcher.Result): Bitmap {
        val output = background.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 2f }
        for (arrow in result.arrows) {
            paint.color = Color.YELLOW
            canvas.drawLine(arrow.reference.x.toFloat(), arrow.reference.y.toFloat(),
                arrow.observed.x.toFloat(), arrow.observed.y.toFloat(), paint)
            paint.color = Color.GREEN
            canvas.drawCircle(arrow.reference.x.toFloat(), arrow.reference.y.toFloat(), 3f, paint)
            paint.color = Color.RED
            canvas.drawCircle(arrow.observed.x.toFloat(), arrow.observed.y.toFloat(), 3f, paint)
        }
        return output
    }
}
