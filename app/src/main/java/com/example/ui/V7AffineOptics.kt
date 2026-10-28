package com.example.ui

import kotlin.math.*

/** Affine distortion of actual scene features, NOT of the screen overlay.
 * All values are dimensionless or pixels; never interpret as diopters without calibration.
 */
internal object V7AffineOptics {
    data class Sample(val x: Double, val y: Double, val dx: Double, val dy: Double)
    data class Result(val m11: Double, val m12: Double, val m21: Double, val m22: Double,
                      val major: Double, val minor: Double, val angleDeg: Double,
                      val residualRmsPx: Double, val samples: Int) {
        fun display(): String = "Mag %.4f / %.4f | Axis %.1f° | fit RMS %.2f px".format(
            major, minor, angleDeg, residualRmsPx)
    }
    fun fit(samples: List<Sample>, centerX: Double, centerY: Double): Result? {
        if (samples.size < 8) return null
        // Least squares affine displacement: dx = a*x+b*y+c, dy = d*x+e*y+f.
        val normal = Array(3) { DoubleArray(3) }
        val bx = DoubleArray(3); val by = DoubleArray(3)
        for (s in samples) {
            val v = doubleArrayOf(s.x-centerX, s.y-centerY, 1.0)
            for (i in 0..2) {
                bx[i] += v[i]*s.dx; by[i] += v[i]*s.dy
                for (j in 0..2) normal[i][j] += v[i]*v[j]
            }
        }
        fun solve(rhs: DoubleArray): DoubleArray? {
            val a = Array(3) { i -> DoubleArray(4) { j -> if (j < 3) normal[i][j] else rhs[i] } }
            for (k in 0..2) {
                val pivot = (k..2).maxByOrNull { abs(a[it][k]) } ?: return null
                if (abs(a[pivot][k]) < 1e-8) return null
                val tmp=a[k]; a[k]=a[pivot]; a[pivot]=tmp
                val divisor=a[k][k]
                for (j in k..3) a[k][j] /= divisor
                for (i in 0..2) if (i != k) {
                    val factor=a[i][k]
                    for (j in k..3) a[i][j] -= factor*a[k][j]
                }
            }
            return DoubleArray(3) { a[it][3] }
        }
        val x = solve(bx) ?: return null; val y = solve(by) ?: return null
        val m11=1+x[0]; val m12=x[1]; val m21=y[0]; val m22=1+y[1]
        // Singular values: principal image magnifications, not optical powers.
        val aa=m11*m11+m21*m21
        val bb=m11*m12+m21*m22
        val dd=m12*m12+m22*m22
        val discr=sqrt(max(0.0,(aa-dd)*(aa-dd)+4*bb*bb))
        val major=sqrt(max(0.0,(aa+dd+discr)/2))
        val minor=sqrt(max(0.0,(aa+dd-discr)/2))
        val angle=(Math.toDegrees(0.5*atan2(2*bb,aa-dd))+180.0)%180.0
        val error=samples.sumOf { s ->
            val u=s.x-centerX; val v=s.y-centerY
            val ex=s.dx-(x[0]*u+x[1]*v+x[2])
            val ey=s.dy-(y[0]*u+y[1]*v+y[2])
            ex*ex+ey*ey
        }
        val rms=sqrt(error/samples.size)
        if (!major.isFinite() || !minor.isFinite() || !rms.isFinite()) return null
        return Result(m11,m12,m21,m22,major,minor,angle,rms,samples.size)
    }
}
