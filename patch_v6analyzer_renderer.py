import re

with open('app/src/main/java/com/example/analysis/v6/V6StructuredDeflectometryAnalyzer.kt', 'r') as f:
    content = f.read()

target = """        val debugBitmap = V6DebugRenderer.renderDebugBitmap(
            width, height, refPoints, lensPoints, refGrid, lensGrid, commonKeys
        )"""

repl = """        val debugBitmap = V6DebugRenderer.renderDebugBitmap(
            width, height, refPoints, lensPoints, refGrid, lensGrid, commonKeys, roi
        )"""

content = content.replace(target, repl)

with open('app/src/main/java/com/example/analysis/v6/V6StructuredDeflectometryAnalyzer.kt', 'w') as f:
    f.write(content)
