package com.example.analysis.v7

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.*

/** Experimental visual test only. Never reports diopters without calibration. */
object SimpleLensTest {
    data class Result(val status: String, val centerX: Double = 0.0,
                      val centerY: Double = 0.0, val radius: Double = 0.0,
                      val insideMatches: Int = 0, val outsideMatches: Int = 0,
                      val image: Bitmap? = null)

    private fun dots(bitmap: Bitmap): List<Point> {
        val rgba = Mat()
        val gray = Mat()
        val binary = Mat()
        val hierarchy = Mat()
        val contours = mutableListOf<MatOfPoint>()
        try {
            Utils.bitmapToMat(bitmap, rgba)
            Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.adaptiveThreshold(gray, binary, 255.0,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2.0)
            Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE)
            return contours.mapNotNull { contour ->
                val area = Imgproc.contourArea(contour)
                if (area < 10.0 || area > 500.0) return@mapNotNull null
                val m = Imgproc.moments(contour)
                if (m.m00 <= 0.0) null else Point(m.m10 / m.m00, m.m01 / m.m00)
            }
        } finally {
            contours.forEach { it.release() }
            rgba.release(); gray.release(); binary.release(); hierarchy.release()
        }
    }

    fun analyze(reference: Bitmap, withLens: Bitmap): Result {
        if (reference.width != withLens.width || reference.height != withLens.height)
            return Result("FRAME_DIMENSIONS_DIFFER")
        val ellipse = com.example.ui.detectLensEllipse(withLens)
            ?: return Result("LENS_BORDER_NOT_FOUND")
        val cx = ellipse.center.x
        val cy = ellipse.center.y
        val rx = ellipse.size.width / 2.0
        val ry = ellipse.size.height / 2.0
        if (rx <= 0.0 || ry <= 0.0 || !cx.isFinite() || !cy.isFinite() ||
            cx < 0 || cy < 0 || cx >= withLens.width || cy >= withLens.height ||
            min(rx, ry) < min(withLens.width, withLens.height) * 0.10 ||
            max(rx, ry) > min(withLens.width, withLens.height) * 0.48 ||
            max(rx, ry) / min(rx, ry) > 1.5)
            return Result("LENS_BORDER_INVALID")

        val ref = dots(reference)
        val lens = dots(withLens)
        val match = SimpleGridMatcher.match(ref, lens)
        if (match.arrows.isEmpty()) return Result("DOT_MATCHING_FAILED")
        // Keep a safe inner zone, excluding refractive rim effects.
        fun normalizedRadius(p: Point): Double =
            hypot((p.x - cx) / rx, (p.y - cy) / ry)
        val inside = match.arrows.filter { normalizedRadius(it.reference) < 0.72 &&
            normalizedRadius(it.observed) < 0.72 }
        val outside = match.arrows.filter { normalizedRadius(it.reference) > 1.18 &&
            normalizedRadius(it.observed) > 1.18 }
        if (outside.size < 15) return Result("INSUFFICIENT_OUTSIDE_REGISTRATION",
            cx, cy, min(rx, ry), inside.size, outside.size)
        // Remove camera/target translation using outside-lens matched dots.
        val tx = outside.map { it.dx }.sorted().let { it[it.size / 2] }
        val ty = outside.map { it.dy }.sorted().let { it[it.size / 2] }
        val output = withLens.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 2.5f }
        paint.color = Color.CYAN
        paint.style = Paint.Style.STROKE
        canvas.drawOval((cx-rx).toFloat(), (cy-ry).toFloat(),
            (cx+rx).toFloat(), (cy+ry).toFloat(), paint)
        paint.style = Paint.Style.FILL
        canvas.drawLine((cx-12).toFloat(), cy.toFloat(), (cx+12).toFloat(), cy.toFloat(), paint)
        canvas.drawLine(cx.toFloat(), (cy-12).toFloat(), cx.toFloat(), (cy+12).toFloat(), paint)
        for (a in inside) {
            val x = a.reference.x.toFloat()
            val y = a.reference.y.toFloat()
            val ex = (a.observed.x-tx).toFloat()
            val ey = (a.observed.y-ty).toFloat()
            paint.color = Color.YELLOW
            canvas.drawLine(x, y, ex, ey, paint)
            paint.color = Color.GREEN
            canvas.drawCircle(x, y, 2.5f, paint)
            paint.color = Color.RED
            canvas.drawCircle(ex, ey, 2.5f, paint)
        }
        val status = if (inside.size < 20) "INSUFFICIENT_INSIDE_MATCHES"
                     else if (!match.accepted) "LOW_GLOBAL_MATCH_COVERAGE"
                     else "VISUAL_TEST_READY_UNCALIBRATED"
        return Result(status, cx, cy, min(rx, ry), inside.size, outside.size, output)
    }
}
