import re

with open('app/src/main/java/com/example/analysis/v6/V6DebugRenderer.kt', 'r') as f:
    content = f.read()

sig_target = r"fun renderDebugBitmap\(\s*width: Int,\s*height: Int,\s*rawRefPoints: List<Point>,\s*rawLensPoints: List<Point>,\s*refGrid: V6GridModel,\s*lensGrid: V6GridModel,\s*commonKeys: Set<Pair<Int, Int>>\s*\): Bitmap \{"
sig_repl = """fun renderDebugBitmap(
        width: Int, 
        height: Int, 
        rawRefPoints: List<Point>,
        rawLensPoints: List<Point>,
        refGrid: V6GridModel, 
        lensGrid: V6GridModel, 
        commonKeys: Set<Pair<Int, Int>>,
        roi: V6LensRoi? = null
    ): Bitmap {"""
content = re.sub(sig_target, sig_repl, content)

drawing_logic = """
        // 4. Draw ROI
        if (roi != null) {
            val paintRoiCenter = Paint().apply { color = Color.RED; strokeWidth = 5f; style = Paint.Style.STROKE }
            val paintRoiInner = Paint().apply { color = Color.BLUE; strokeWidth = 5f; style = Paint.Style.STROKE }
            val paintRoiOuter = Paint().apply { color = Color.GREEN; strokeWidth = 5f; style = Paint.Style.STROKE }
            
            canvas.drawLine((roi.centerX - 20).toFloat(), roi.centerY.toFloat(), (roi.centerX + 20).toFloat(), roi.centerY.toFloat(), paintRoiCenter)
            canvas.drawLine(roi.centerX.toFloat(), (roi.centerY - 20).toFloat(), roi.centerX.toFloat(), (roi.centerY + 20).toFloat(), paintRoiCenter)
            
            canvas.drawCircle(roi.centerX.toFloat(), roi.centerY.toFloat(), roi.innerRadius.toFloat(), paintRoiInner)
            canvas.drawCircle(roi.centerX.toFloat(), roi.centerY.toFloat(), roi.outerRadius.toFloat(), paintRoiOuter)
        }
        
        // 5. Draw Image Bounds
        val paintBounds = Paint().apply { color = Color.MAGENTA; strokeWidth = 8f; style = Paint.Style.STROKE }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintBounds)
        
        val paintText = Paint().apply { color = Color.MAGENTA; textSize = 60f; style = Paint.Style.FILL }
        canvas.drawText("${width} x ${height}", 50f, 100f, paintText)
        
        return bitmap"""

content = content.replace("return bitmap", drawing_logic)

with open('app/src/main/java/com/example/analysis/v6/V6DebugRenderer.kt', 'w') as f:
    f.write(content)
