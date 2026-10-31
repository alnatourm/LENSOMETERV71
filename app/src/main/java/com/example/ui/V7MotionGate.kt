package com.example.ui

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video
import kotlin.math.hypot

/** Frame-to-frame handheld stability detection. Not a substitute for optical-fit confidence. */
internal class V7MotionGate {
    private var previous: Mat? = null
    private var stableFrames = 0
    fun reset() { previous?.release(); previous = null; stableFrames = 0 }
    fun inspect(bitmap: Bitmap): Pair<Boolean, String> {
        val rgba=Mat(); val gray=Mat(); val small=Mat()
        try {
            Utils.bitmapToMat(bitmap,rgba)
            Imgproc.cvtColor(rgba,gray,Imgproc.COLOR_RGBA2GRAY)
            Imgproc.resize(gray,small,Size(320.0,240.0))
            val prior=previous
            previous=small.clone()
            if (prior == null) return false to "Hold still: stabilizing"
            val corners=MatOfPoint()
            val a=MatOfPoint2f(); val b=MatOfPoint2f()
            val flags=MatOfByte(); val errors=MatOfFloat()
            try {
                Imgproc.goodFeaturesToTrack(prior,corners,100,0.02,8.0)
                val pts=corners.toArray()
                if (pts.size < 18) { stableFrames=0; return false to "Aim at a detailed scene" }
                a.fromArray(*pts)
                Video.calcOpticalFlowPyrLK(prior,small,a,b,flags,errors)
                val next=b.toArray(); val valid=flags.toArray()
                val distances=pts.indices.mapNotNull { i ->
                    if (i>=next.size || i>=valid.size || valid[i].toInt()==0) null
                    else hypot(next[i].x-pts[i].x,next[i].y-pts[i].y)
                }.sorted()
                if (distances.size < 15) { stableFrames=0; return false to "Hold still: tracking lost" }
                val motion=distances[distances.size/2]
                if (motion > 0.75) stableFrames=0 else stableFrames++
                val ready=stableFrames>=10
                return ready to if (ready) "Camera stable" else "Hold still (${stableFrames}/10)"
            } finally {
                prior.release(); corners.release(); a.release(); b.release(); flags.release(); errors.release()
            }
        } finally { rgba.release(); gray.release(); small.release() }
    }
}
