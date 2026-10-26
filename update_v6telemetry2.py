import re

with open('app/src/main/java/com/example/analysis/v6/V6Telemetry.kt', 'r') as f:
    content = f.read()

new_fields = """
    val analysisImageWidth: Int = 0,
    val analysisImageHeight: Int = 0,

    val rawRefMinX: Double = 0.0,
    val rawRefMaxX: Double = 0.0,
    val rawRefMinY: Double = 0.0,
    val rawRefMaxY: Double = 0.0,

    val assignedRefMinX: Double = 0.0,
    val assignedRefMaxX: Double = 0.0,
    val assignedRefMinY: Double = 0.0,
    val assignedRefMaxY: Double = 0.0,

    val assignedLensMinX: Double = 0.0,
    val assignedLensMaxX: Double = 0.0,
    val assignedLensMinY: Double = 0.0,
    val assignedLensMaxY: Double = 0.0,

    val commonMinX: Double = 0.0,
    val commonMaxX: Double = 0.0,
    val commonMinY: Double = 0.0,
    val commonMaxY: Double = 0.0,

    val nearestRefDistanceToRoiCenter: Double = 0.0,
    val nearestLensDistanceToRoiCenter: Double = 0.0,
    val nearestCommonDistanceToRoiCenter: Double = 0.0,
"""

content = content.replace('val autoRoiRejectReason: String = "",', 'val autoRoiRejectReason: String = "",\n' + new_fields)

with open('app/src/main/java/com/example/analysis/v6/V6Telemetry.kt', 'w') as f:
    f.write(content)
