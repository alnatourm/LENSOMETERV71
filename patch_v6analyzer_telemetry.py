import re

with open('app/src/main/java/com/example/analysis/v6/V6StructuredDeflectometryAnalyzer.kt', 'r') as f:
    content = f.read()

# I need to add telemetry generation logic just before creating `val telemetry = V6Telemetry(...)`

telemetry_block_start = "val telemetry = V6Telemetry("
telemetry_logic = """
        // Distance helper
        fun dist(pt: Point, cx: Double, cy: Double) = Math.sqrt((pt.x - cx) * (pt.x - cx) + (pt.y - cy) * (pt.y - cy))

        val assignedRefPts = refGrid.points.values.mapNotNull { it.observedPosition }
        val assignedLensPts = lensGrid.points.values.mapNotNull { it.observedPosition }
        val commonPts = commonKeys.mapNotNull { refGrid.points[it]?.observedPosition }

        val nearestRef = assignedRefPts.minOfOrNull { dist(it, roi.centerX, roi.centerY) } ?: -1.0
        val nearestLens = assignedLensPts.minOfOrNull { dist(it, roi.centerX, roi.centerY) } ?: -1.0
        val nearestCommon = commonPts.minOfOrNull { dist(it, roi.centerX, roi.centerY) } ?: -1.0

        val telemetry = V6Telemetry(
            analysisImageWidth = width,
            analysisImageHeight = height,
            rawRefMinX = refPoints.minOfOrNull { it.x } ?: 0.0,
            rawRefMaxX = refPoints.maxOfOrNull { it.x } ?: 0.0,
            rawRefMinY = refPoints.minOfOrNull { it.y } ?: 0.0,
            rawRefMaxY = refPoints.maxOfOrNull { it.y } ?: 0.0,
            assignedRefMinX = assignedRefPts.minOfOrNull { it.x } ?: 0.0,
            assignedRefMaxX = assignedRefPts.maxOfOrNull { it.x } ?: 0.0,
            assignedRefMinY = assignedRefPts.minOfOrNull { it.y } ?: 0.0,
            assignedRefMaxY = assignedRefPts.maxOfOrNull { it.y } ?: 0.0,
            assignedLensMinX = assignedLensPts.minOfOrNull { it.x } ?: 0.0,
            assignedLensMaxX = assignedLensPts.maxOfOrNull { it.x } ?: 0.0,
            assignedLensMinY = assignedLensPts.minOfOrNull { it.y } ?: 0.0,
            assignedLensMaxY = assignedLensPts.maxOfOrNull { it.y } ?: 0.0,
            commonMinX = commonPts.minOfOrNull { it.x } ?: 0.0,
            commonMaxX = commonPts.maxOfOrNull { it.x } ?: 0.0,
            commonMinY = commonPts.minOfOrNull { it.y } ?: 0.0,
            commonMaxY = commonPts.maxOfOrNull { it.y } ?: 0.0,
            nearestRefDistanceToRoiCenter = nearestRef,
            nearestLensDistanceToRoiCenter = nearestLens,
            nearestCommonDistanceToRoiCenter = nearestCommon,
"""

content = content.replace("val telemetry = V6Telemetry(", telemetry_logic)

with open('app/src/main/java/com/example/analysis/v6/V6StructuredDeflectometryAnalyzer.kt', 'w') as f:
    f.write(content)
