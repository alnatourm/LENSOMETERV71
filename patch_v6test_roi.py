import re

with open('app/src/test/java/com/example/analysis/v6/V6SyntheticTest.kt', 'r') as f:
    content = f.read()

new_test = """
    @Test
    fun testRoiCoordinateSpaceMatch() {
        val roi = V6LensRoi(320.0, 240.0, 120.0, 192.0)
        
        // Inside
        assertEquals(V6CellRegion.INSIDE_LENS, roi.classify(Point(320.0, 240.0)))
        assertEquals(V6CellRegion.INSIDE_LENS, roi.classify(Point(350.0, 240.0)))
        assertEquals(V6CellRegion.INSIDE_LENS, roi.classify(Point(320.0, 300.0)))
        
        // Outside inner ROI (Boundary or Outside)
        assertNotEquals(V6CellRegion.INSIDE_LENS, roi.classify(Point(500.0, 240.0)))
    }
"""

content = content.replace("    @Test\n    fun testRoiValidationNegativeCenter() {", new_test + "\n    @Test\n    fun testRoiValidationNegativeCenter() {")

with open('app/src/test/java/com/example/analysis/v6/V6SyntheticTest.kt', 'w') as f:
    f.write(content)
