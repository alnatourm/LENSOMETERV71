package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

/** Fixed Hermann-style screen grid plus best-effort camera lens-rim detection. */
internal object V7GridAndLensOverlay {
    fun render(cameraFrame: Bitmap): Pair<Bitmap, String> {
        val output = cameraFrame.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        val width = output.width.toFloat()
        val height = output.height.toFloat()
        val pitch = (minOf(width, height) / 12f).coerceAtLeast(12f)
        paint.color = Color.WHITE
        paint.alpha = 175
        paint.strokeWidth = 1.4f
        var x = width / 2f
        while (x <= width) { canvas.drawLine(x, 0f, x, height, paint); x += pitch }
        x = width / 2f - pitch
        while (x >= 0f) { canvas.drawLine(x, 0f, x, height, paint); x -= pitch }
        var y = height / 2f
        while (y <= height) { canvas.drawLine(0f, y, width, y, paint); y += pitch }
        y = height / 2f - pitch
        while (y >= 0f) { canvas.drawLine(0f, y, width, y, paint); y -= pitch }
        val rgba = Mat()
        val gray = Mat()
        val blurred = Mat()
        val edges = Mat()
        val hierarchy = Mat()
        val contours = mutableListOf<MatOfPoint>()
        try {
            Utils.bitmapToMat(cameraFrame, rgba)
            Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.GaussianBlur(gray, blurred, Size(7.0, 7.0), 1.8)
            Imgproc.Canny(blurred, edges, 55.0, 140.0)
            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
            val minArea = width * height * 0.025
            val maxArea = width * height * 0.85
            var best: RotatedRect? = null
            var bestScore = 0.0
            for (contour in contours) {
                if (contour.rows() < 12) continue
                val area = abs(Imgproc.contourArea(contour))
                if (area < minArea || area > maxArea) continue
                val points = MatOfPoint2f(*contour.toArray())
                try {
                    val ellipse = Imgproc.fitEllipse(points)
                    val a = ellipse.size.width
                    val b = ellipse.size.height
                    if (a <= 0 || b <= 0 || minOf(a,b) / maxOf(a,b) < 0.50) continue
                    val ellipseArea = Math.PI * a * b / 4.0
                    val ratio = area / ellipseArea
                    if (ratio !in 0.65..1.35) continue
                    if (ellipse.center.x !in 0.0..width.toDouble() || ellipse.center.y !in 0.0..height.toDouble()) continue
                    val score = area * (1.0 - abs(1.0-ratio))
                    if (score > bestScore) { bestScore = score; best = ellipse }
                } finally { points.release() }
            }
            val rim = best
            if (rim != null) {
                paint.color = Color.CYAN
                paint.alpha = 255
                paint.strokeWidth = 4f
                canvas.save()
                canvas.rotate(rim.angle.toFloat(), rim.center.x.toFloat(), rim.center.y.toFloat())
                canvas.drawOval(RectF(
                    (rim.center.x-rim.size.width/2).toFloat(),
                    (rim.center.y-rim.size.height/2).toFloat(),
                    (rim.center.x+rim.size.width/2).toFloat(),
                    (rim.center.y+rim.size.height/2).toFloat()), paint)
                canvas.restore()
                paint.color = Color.YELLOW
                val cx = rim.center.x.toFloat(); val cy = rim.center.y.toFloat()
                canvas.drawLine(cx-20,cy,cx+20,cy,paint)
                canvas.drawLine(cx,cy-20,cx,cy+20,paint)
                return output to "Lens rim candidate; center: (${cx.toInt()}, ${cy.toInt()})"
            }
            return output to "Lens edge not detected; keep full rim visible"
        } finally {
            contours.forEach { it.release() }
            rgba.release(); gray.release(); blurred.release(); edges.release(); hierarchy.release()
        }
    }
}
