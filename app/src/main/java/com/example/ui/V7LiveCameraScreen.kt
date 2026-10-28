package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.hypot

/**
 * Camera-only experimental optical-flow filter.
 * Tracks natural scene features, not digital overlay dots.
 * Keep the phone and scene stationary; no prescription is inferred.
 */
@Composable
fun V7LiveCameraScreen() {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var permission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission = it }
    LaunchedEffect(Unit) { if (!permission) request.launch(Manifest.permission.CAMERA) }
    var display by remember { mutableStateOf<Bitmap?>(null) }
    var status by remember { mutableStateOf("Point at a detailed, stationary scene") }
    var referenceCount by remember { mutableIntStateOf(0) }
    val setReference = remember { AtomicBoolean(false) }
    val reset = remember { AtomicBoolean(false) }
    val previewView = remember { PreviewView(context) }
    DisposableEffect(owner, permission) {
        if (!permission) return@DisposableEffect onDispose {}
        val executor = Executors.newSingleThreadExecutor()
        val future = ProcessCameraProvider.getInstance(context)
        val engine = LiveFeatureEngine()
        val listener = Runnable {
            try {
                val provider = future.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { frame ->
                    try {
                        if (reset.getAndSet(false)) engine.clear()
                        val bitmap = v4ProxyToBitmap(frame)
                        if (bitmap != null) {
                            val result = engine.process(bitmap, setReference.getAndSet(false))
                            ContextCompat.getMainExecutor(context).execute {
                                display = result.first
                                status = result.second
                                referenceCount = engine.count
                            }
                        }
                    } catch (_: Exception) {
                        ContextCompat.getMainExecutor(context).execute { status = "Tracking error: reset reference" }
                    } finally { frame.close() }
                }
                provider.unbindAll()
                provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            } catch (_: Exception) {
                status = "Camera unavailable"
            }
        }
        future.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose {
            if (future.isDone) try { future.get().unbindAll() } catch (_: Exception) {}
            executor.shutdown()
            engine.clear()
        }
    }
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            display?.let { bmp ->
                Image(bitmap = bmp.asImageBitmap(), contentDescription = "Live tracked feature dots and displacement arrows",
                    modifier = Modifier.fillMaxSize())
            }
        }
        Text("V7 CAMERA-ONLY • EXPERIMENTAL", modifier = Modifier.padding(8.dp))
        Text(status, modifier = Modifier.padding(horizontal = 8.dp))
        Text("Reference features: $referenceCount | SPH/CYL/AXIS: N/A", modifier = Modifier.padding(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Button(onClick = { setReference.set(true) }, modifier = Modifier.weight(1f)) { Text("SET REFERENCE") }
            Button(onClick = { reset.set(true); display = null }, modifier = Modifier.weight(1f)) { Text("RESET") }
        }
    }
}

private class LiveFeatureEngine {
    private var referenceGray: Mat? = null
    private var referenceFeatures: MatOfPoint2f? = null
    var count = 0
        private set

    fun clear() {
        referenceGray?.release(); referenceGray = null
        referenceFeatures?.release(); referenceFeatures = null
        count = 0
    }

    fun process(bitmap: Bitmap, capture: Boolean): Pair<Bitmap, String> {
        val rgba = Mat()
        val gray = Mat()
        try {
            Utils.bitmapToMat(bitmap, rgba)
            Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
            if (capture) {
                clear()
                val corners = MatOfPoint()
                Imgproc.goodFeaturesToTrack(gray, corners, 250, 0.02, 12.0)
                val pts = corners.toArray()
                corners.release()
                referenceGray = gray.clone()
                referenceFeatures = MatOfPoint2f(*pts)
                count = pts.size
            }
            val refGray = referenceGray ?: return bitmap to "Tap SET REFERENCE without the lens"
            val refPoints = referenceFeatures ?: return bitmap to "Set reference first"
            if (count < 25) return bitmap to "Too few scene features. Aim at a textured scene."
            val tracked = MatOfPoint2f()
            val status = MatOfByte()
            val errors = MatOfFloat()
            try {
                Video.calcOpticalFlowPyrLK(refGray, gray, refPoints, tracked, status, errors)
                val original = refPoints.toArray()
                val current = tracked.toArray()
                val valid = status.toArray()
                val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(output)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 2.5f }
                var n = 0
                val distances = mutableListOf<Double>()
                for (i in original.indices) {
                    if (i >= valid.size || i >= current.size || valid[i].toInt() == 0) continue
                    val p = original[i]; val q = current[i]
                    val d = hypot(q.x - p.x, q.y - p.y)
                    if (!d.isFinite() || d > 80.0) continue
                    n++; distances.add(d)
                    paint.color = AndroidColor.YELLOW
                    canvas.drawLine(p.x.toFloat(), p.y.toFloat(), q.x.toFloat(), q.y.toFloat(), paint)
                    paint.color = AndroidColor.GREEN
                    canvas.drawCircle(p.x.toFloat(), p.y.toFloat(), 3f, paint)
                    paint.color = AndroidColor.RED
                    canvas.drawCircle(q.x.toFloat(), q.y.toFloat(), 3f, paint)
                }
                val median = distances.sorted().let { if (it.isEmpty()) 0.0 else it[it.size / 2] }
                val message = "Tracked: $n/$count | Median shift: %.1f px | Raw, uncalibrated".format(median)
                return output to message
            } finally {
                tracked.release(); status.release(); errors.release()
            }
        } finally { rgba.release(); gray.release() }
    }
}
