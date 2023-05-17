package org.prismsus.tank.utils.tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.prismsus.tank.utils.*
import kotlin.math.*
class LineTest {

    @Test
    fun intersect() {
        // test if two same lines can intersect
        var line1 = Line(DPos2.ORIGIN, DPos2(1.0, 1.0))
        assertTrue(line1.inter == 0.0)
        assertTrue(line1.slope == 1.0)
        var line2 = Line(DPos2.ORIGIN, DPos2(1.0, 1.0))
        assertTrue(line1 intersect line2)
        assertTrue(line2.intersect(line1))
        // test the case where two lines are parallel, but not intersect within their range
        line1 = Line(DPos2.ORIGIN, DPos2(1.0, 1.0))
        line2 = Line(DPos2(0.0, 1.0), DPos2(1.0, 2.0))
        assertFalse(line1.intersect(line2))
        assertFalse(line2.intersect(line1))
        // test the case where there are two lines with different slope, and should intersect
        line1 = Line(DPos2.ORIGIN, DPos2(1.0, 1.0))
        line2 = Line(DPos2(0.0, 1.0), DPos2(1.0, 0.0))
        assertTrue(line1.intersect(line2))
        assertTrue(line2.intersect(line1))
        // test the case where there are two lines with different slope
        // and will intersect, but outside the endpoints
        line1 = Line(DPos2.ORIGIN, DPos2(1.0, 1.0))
        line2 = Line(DPos2(0.0, 2.0), DPos2(1.0, 1.1))
        assertFalse(line1.intersect(line2))
        assertFalse(line2.intersect(line1))
        // case where the line are touching
        line1 = Line(DPos2.ORIGIN, DPos2(1.0, 1.0))
        line2 = Line(DPos2(0.0, 2.0), DPos2(1.0, 1.0))
        assertTrue(line2.inter == 2.0)
        assertTrue(line2.slope == -1.0)
        assertTrue(line1.inter == 0.0)
        assertTrue(line1.slope == 1.0)
        assertTrue(line1.intersect(line2))
        assertTrue(line2.intersect(line1))
        // cases when the lines are vertical
        line1 = Line(DPos2.ORIGIN, DPos2.UP)
        line2 = Line((DVec2.ORIGIN + DVec2.LF * .1).toPt(), DPos2.RT + DVec2.UP * .1)
        assertTrue(line1.intersect(line2))
        assertTrue(line2.intersect(line1))
    }

    @Test
    fun rotate() {
        // rotate a line by 90 degree, from flat line to vertical
        var line = Line(DPos2.ORIGIN, DPos2(1.0, 0.0))
        var lineRotated = Line(DPos2.ORIGIN, DPos2(0.0, 1.0))
        assertTrue(line.rotateDeg(90.0, DPos2.ORIGIN) == lineRotated)

        // rotate a line by -90 degree, from flat to vertical down
        lineRotated = Line(DPos2.ORIGIN, DPos2(0.0, -1.0))
        assertTrue(line.rotateDeg(-90.0, DPos2.ORIGIN) == lineRotated)

        // rotate a line by 90 degree, but centered at the middle of the line (0.5, 0.0)
        lineRotated = Line(DPos2(0.5, -0.5), DPos2(0.5, 0.5))
        assertTrue(line.rotateDeg(90.0, DPos2(0.5, 0.0)) == lineRotated)

        // rotate a line by a randomly generated angle
        val rad = Math.random() * PI / 2
        lineRotated = Line(DPos2.ORIGIN, DPos2(cos(rad), sin(rad)))
        assertTrue(line.rotate(rad, DPos2.ORIGIN) == lineRotated)
    }


    @Test
    fun angleRotated(){
        // rotate the line from the left end point of a line
        // and then test if the angle is correct under the perspective of the middle point
        // (they should be the same in terms of line)
        var line = Line(DPos2.ORIGIN, DPos2(1.0, 0.0))
        var lineRotatedByCenter = line.copy().rotate(90.0.toRad())
        // default second parameter is by center
        println(lineRotatedByCenter.angleRotated.toDeg())
        var lineRotatedByLeft = line.copy().rotate(90.0.toRad(), DPos2.ORIGIN)
        assertEquals(lineRotatedByCenter.angleRotated, lineRotatedByLeft.angleRotated, DOUBLE_PRECISION)

        // rotate the line from the right end point of a line
        var lineRotatedByRight = line.copy().rotate(90.0.toRad(), DPos2(1.0, 0.0))
        assertEquals(lineRotatedByCenter.angleRotated, lineRotatedByRight.angleRotated, DOUBLE_PRECISION)
    }
}