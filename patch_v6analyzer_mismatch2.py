import re

with open('app/src/main/java/com/example/analysis/v6/V6StructuredDeflectometryAnalyzer.kt', 'r') as f:
    content = f.read()

target = """            failureReason = if (refGrid.validCellCount > 100 && lensGrid.validCellCount > 100 && commonKeys.size > 100 && insideLensRefCells == 0 && insideLensLensCells == 0) {
                "ROI_COORDINATE_SPACE_MISMATCH"
            } else if (acceptedMeasurements.isEmpty()) {"""
repl = """            failureReason = if (refGrid.validCellCount > 100 && lensGrid.validCellCount > 100 && commonKeys.size > 100 && insideLensRefCells == 0 && insideLensLensCells == 0) {
                "ROI_COORDINATE_SPACE_MISMATCH: roi=(${roi.centerX},${roi.centerY},R=${roi.innerRadius}), refPts=(${nearestRef})"
            } else if (acceptedMeasurements.isEmpty()) {"""
content = content.replace(target, repl)

with open('app/src/main/java/com/example/analysis/v6/V6StructuredDeflectometryAnalyzer.kt', 'w') as f:
    f.write(content)
