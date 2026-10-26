import re

with open('app/src/main/java/com/example/analysis/v6/V6StructuredDeflectometryAnalyzer.kt', 'r') as f:
    content = f.read()

target = """            success = acceptedMeasurements.isNotEmpty(),
            failureReason = if (acceptedMeasurements.isEmpty()) "No accepted vectors" else "","""
repl = """            success = acceptedMeasurements.isNotEmpty(),
            failureReason = if (refGrid.validCellCount > 100 && lensGrid.validCellCount > 100 && commonKeys.size > 100 && insideLensRefCells == 0 && insideLensLensCells == 0) {
                "ROI_COORDINATE_SPACE_MISMATCH"
            } else if (acceptedMeasurements.isEmpty()) {
                "No accepted vectors"
            } else {
                ""
            },"""
content = content.replace(target, repl)

with open('app/src/main/java/com/example/analysis/v6/V6StructuredDeflectometryAnalyzer.kt', 'w') as f:
    f.write(content)
