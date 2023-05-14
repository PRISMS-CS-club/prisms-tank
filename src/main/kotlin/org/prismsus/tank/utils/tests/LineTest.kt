package org.prismsus.tank.utils.tests

import org.junit.jupiter.api.Assertions.*
import org.prismsus.tank.utils.DPos2
import org.prismsus.tank.utils.DVec2
import org.prismsus.tank.utils.Line
import kotlin.math.*
class LineTest {

    @org.junit.jupiter.api.Test
    fun intersect() {
        // test if two same lines can intersect
        var line1 = Line(DVec2.ORIGIN, DPos2(1.0, 1.0))
        assertTrue(line1.inter == 0.0)
        assertTrue(line1.slope == 1.0)
        var line2 = Line(DVec2.ORIGIN, DPos2(1.0, 1.0))
        assertTrue(line1.intersect(line2))
        assertTrue(line2.intersect(line1))
        // test the case where two lines are parallel, but not intersect within their range
        line1 = Line(DVec2.ORIGIN, DPos2(1.0, 1.0))
        line2 = Line(DPos2(0.0, 1.0), DPos2(1.0, 2.0))
        assertFalse(line1.intersect(line2))
        assertFalse(line2.intersect(line1))
        // test the case where there are two lines with different slope, and should intersect
        line1 = Line(DVec2.ORIGIN, DPos2(1.0, 1.0))
        line2 = Line(DPos2(0.0, 1.0), DPos2(1.0, 0.0))
        assertTrue(line1.intersect(line2))
        assertTrue(line2.intersect(line1))
        // test the case where there are two lines with different slope
        // and will intersect, but outside the endpoints
        line1 = Line(DVec2.ORIGIN, DPos2(1.0, 1.0))
        line2 = Line(DPos2(0.0, 2.0), DPos2(1.0, 1.1))
        assertFalse(line1.intersect(line2))
        assertFalse(line2.intersect(line1))
        // case where the line are touching
        line1 = Line(DVec2.ORIGIN, DPos2(1.0, 1.0))
        line2 = Line(DPos2(0.0, 2.0), DPos2(1.0, 1.0))
        assertTrue(line2.inter == 2.0)
        assertTrue(line2.slope == -1.0)
        assertTrue(line1.inter == 0.0)
        assertTrue(line1.slope == 1.0)
        assertTrue(line1.intersect(line2))
        assertTrue(line2.intersect(line1))
        // cases when the lines are vertical
        line1 = Line(DVec2.ORIGIN, DPos2.UP)
        line2 = Line(DVec2.ORIGIN + DVec2.LF * .1, DPos2.RT + DVec2.UP * .1)
        assertTrue(line1.intersect(line2))
        assertTrue(line2.intersect(line1))
    }

    @org.junit.jupiter.api.Test
    fun rotate() {
        // rotate a line by 90 degree, from flat line to vertical
        var line = Line(DVec2.ORIGIN, DPos2(1.0, 0.0))
        var lineRotated = Line(DVec2.ORIGIN, DPos2(0.0, 1.0))
        assertTrue(line.rotateDeg(DVec2.ORIGIN, 90.0) == lineRotated)

        // rotate a line by -90 degree, from flat to vertical down
        lineRotated = Line(DVec2.ORIGIN, DPos2(0.0, -1.0))
        assertTrue(line.rotateDeg(DVec2.ORIGIN, -90.0) == lineRotated)

        // rotate a line by 90 degree, but centered at the middle of the line (0.5, 0.0)
        lineRotated = Line(DPos2(0.5, -0.5), DPos2(0.5, 0.5))
        assertTrue(line.rotateDeg(DPos2(0.5, 0.0), 90.0) == lineRotated)

        // rotate a line by a randomly generated angle
        val rad = Math.random() * PI / 2
        lineRotated = Line(DVec2.ORIGIN, DPos2(cos(rad), sin(rad)))
        assertTrue(line.rotate(DVec2.ORIGIN, rad) == lineRotated)
    }
}