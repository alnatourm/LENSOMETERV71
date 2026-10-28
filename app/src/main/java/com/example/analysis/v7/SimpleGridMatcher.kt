package com.example.analysis.v7

import org.opencv.core.Point
import kotlin.math.*

/**
 * KISS experimental matching: reference image -> lens image.
 * Requires a stationary camera and target, and movement smaller than half
 * the grid pitch. No optical power is inferred from these correspondences.
 */
object SimpleGridMatcher {
    data class Arrow(val reference: Point, val observed: Point, val dx: Double, val dy: Double)
    data class Result(val arrows: List<Arrow>, val spacingPx: Double, val coverage: Double,
                      val accepted: Boolean, val reason: String)

    fun match(reference: List<Point>, observed: List<Point>): Result {
        if (reference.size < 25 || observed.size < 25)
            return Result(emptyList(), 0.0, 0.0, false, "NOT_ENOUGH_DOTS")
        // Nearest-neighbor spacing from the reference image, independent of grid topology.
        val pitches = reference.mapIndexedNotNull { i, p ->
            reference.indices.asSequence().filter { it != i }
                .map { j -> hypot(p.x-reference[j].x, p.y-reference[j].y) }
                .filter { it > 1e-6 }.minOrNull()
        }.sorted()
        if (pitches.isEmpty()) return Result(emptyList(), 0.0, 0.0, false, "NO_GRID_SPACING")
        val pitch = pitches[pitches.size / 2]
        if (!pitch.isFinite() || pitch < 3.0)
            return Result(emptyList(), pitch, 0.0, false, "GRID_TOO_SMALL")

        // Strict mutual nearest-neighbor matching. Half-pitch gate avoids
        // accidentally assigning the next dot on a repetitive grid.
        val gate = pitch * 0.45
        fun closest(p: Point, candidates: List<Point>): Pair<Int, Double> {
            var best = -1
            var dist = Double.POSITIVE_INFINITY
            for (i in candidates.indices) {
                val d = hypot(p.x-candidates[i].x, p.y-candidates[i].y)
                if (d < dist) { best = i; dist = d }
            }
            return best to dist
        }
        val arrows = mutableListOf<Arrow>()
        for (p in reference) {
            val (j, d) = closest(p, observed)
            if (j < 0 || d > gate) continue
            val (iBack, _) = closest(observed[j], reference)
            if (iBack < 0 || reference[iBack] !== p) continue
            val q = observed[j]
            arrows.add(Arrow(p, q, q.x-p.x, q.y-p.y))
        }
        val coverage = arrows.size.toDouble() / reference.size
        val accepted = coverage >= 0.60
        return Result(arrows, pitch, coverage, accepted,
            if (accepted) "OK" else "LOW_COVERAGE_OR_LARGE_MOVEMENT")
    }
}
