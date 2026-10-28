package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video
import kotlin.math.abs
import kotlin.math.hypot

/** Measure displacement strictly inside the detected lens, corrected by outside-frame motion. */
internal object V7InsideLensMeasurement {
    fun analyze(reference: Mat, points: MatOfPoint2f, frame: Bitmap): Pair<Bitmap, String> {
        val rgba = Mat(); val gray = Mat(); val edges = Mat(); val blurred = Mat(); val hierarchy = Mat()
        val contours = mutableListOf<MatOfPoint>()
        val tracked = MatOfPoint2f(); val status = MatOfByte(); val errors = MatOfFloat()
        try {
            Utils.bitmapToMat(frame, rgba)
            Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.GaussianBlur(gray, blurred, Size(7.0,7.0), 1.8)
            Imgproc.Canny(blurred, edges, 55.0, 140.0)
            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
            val areaFrame = frame.width.toDouble() * frame.height
            var rim: RotatedRect? = null; var bestScore = 0.0
            for (contour in contours) {
                if (contour.rows() < 12) continue
                val area = abs(Imgproc.contourArea(contour))
                if (area < areaFrame * 0.025 || area > areaFrame * 0.85) continue
                val pts = MatOfPoint2f(*contour.toArray())
                try {
                    val e = Imgproc.fitEllipse(pts)
                    val a = e.size.width; val b = e.size.height
                    if (a <= 0 || b <= 0 || minOf(a,b)/maxOf(a,b) < 0.50) continue
                    val ratio = area / (Math.PI*a*b/4.0)
                    if (ratio !in 0.65..1.35) continue
                    if (e.center.x !in 0.0..frame.width.toDouble() || e.center.y !in 0.0..frame.height.toDouble()) continue
                    val score = area * (1.0 - abs(1.0-ratio))
                    if (score > bestScore) { bestScore = score; rim = e }
                } finally { pts.release() }
            }
            val ellipse = rim ?: return frame to "No reliable lens border: inside measurement paused"
            Video.calcOpticalFlowPyrLK(reference, gray, points, tracked, status, errors)
            val original = points.toArray(); val current = tracked.toArray(); val flags = status.toArray()
            val inside = mutableListOf<Pair<Point,Point>>()
            val outside = mutableListOf<Pair<Point,Point>>()
            val angle = Math.toRadians(ellipse.angle)
            val cos = kotlin.math.cos(angle); val sin = kotlin.math.sin(angle)
            fun radius(p: Point): Double {
                val dx = p.x-ellipse.center.x; val dy = p.y-ellipse.center.y
                val u = (cos*dx+sin*dy)/(ellipse.size.width/2.0)
                val v = (-sin*dx+cos*dy)/(ellipse.size.height/2.0)
                return hypot(u,v)
            }
            for (i in original.indices) {
                if (i >= current.size || i >= flags.size || flags[i].toInt() == 0) continue
                val p = original[i]; val q = current[i]
                if (hypot(q.x-p.x,q.y-p.y) > 80) continue
                val r = radius(q)
                if (r < 0.75 && radius(p) < 0.95) inside.add(p to q)
                else if (r > 1.25 && radius(p) > 1.25) outside.add(p to q)
            }
            if (outside.size < 5) return frame to "Outside reference insufficient: hold camera still"
            fun median(values: List<Double>): Double = values.sorted()[values.size/2]
            val motionX = median(outside.map { it.second.x-it.first.x })
            val motionY = median(outside.map { it.second.y-it.first.y })
            if (inside.size < 5) return frame to "Inside lens: ${inside.size} points, need at least 5"
            val dx = inside.map { it.second.x-it.first.x-motionX }
            val dy = inside.map { it.second.y-it.first.y-motionY }
            val meanX = dx.average(); val meanY = dy.average()
            val rms = kotlin.math.sqrt(inside.indices.map { dx[it]*dx[it]+dy[it]*dy[it] }.average())
            val affine = V7AffineOptics.fit(inside.indices.map { i ->
                V7AffineOptics.Sample(inside[i].first.x, inside[i].first.y, dx[i], dy[i])
            }, ellipse.center.x, ellipse.center.y)
            val output = frame.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth=2.5f; color=Color.YELLOW }
            inside.forEachIndexed { i, pair ->
                canvas.drawLine(pair.first.x.toFloat(),pair.first.y.toFloat(),
                    (pair.first.x+dx[i]).toFloat(),(pair.first.y+dy[i]).toFloat(),paint)
            }
            return output to ("INSIDE ${inside.size} | X %.1f px | Y %.1f px | shift RMS %.1f px | ".format(meanX,meanY,rms) +
                (affine?.display() ?: "Affine fit unavailable"))
        } finally {
            contours.forEach { it.release() }
            rgba.release();gray.release();edges.release();blurred.release();hierarchy.release()
            tracked.release();status.release();errors.release()
        }
    }
}
