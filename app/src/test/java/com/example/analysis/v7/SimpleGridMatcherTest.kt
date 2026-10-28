package com.example.analysis.v7

import org.junit.Assert.*
import org.junit.Test
import org.opencv.core.Point

class SimpleGridMatcherTest {
    private fun grid() = (0 until 20).flatMap { y ->
        (0 until 25).map { x -> Point(20.0 + x * 15.0, 20.0 + y * 15.0) }
    }

    @Test fun matchesFiveHundredDotsWithoutTopology() {
        val reference = grid()
        val observed = reference.map { Point(it.x + 1.2, it.y - 0.8) }
        val result = SimpleGridMatcher.match(reference, observed)
        assertTrue(result.reason, result.accepted)
        assertEquals(500, result.arrows.size)
        assertEquals(1.2, result.arrows[0].dx, 1e-6)
        assertEquals(-0.8, result.arrows[0].dy, 1e-6)
    }

    @Test fun rejectsLargeAmbiguousMovement() {
        val reference = grid()
        val observed = reference.map { Point(it.x + 7.0, it.y) }
        val result = SimpleGridMatcher.match(reference, observed)
        assertFalse(result.accepted)
        assertEquals(0, result.arrows.size)
    }

    @Test fun rejectsInsufficientCoverage() {
        val reference = grid()
        val observed = reference.take(100)
        val result = SimpleGridMatcher.match(reference, observed)
        assertFalse(result.accepted)
    }
}
