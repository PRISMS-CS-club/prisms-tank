package org.prismsus.tank.utils.tests

import org.junit.jupiter.api.Test
import org.prismsus.tank.utils.*
import org.junit.jupiter.api.Assertions.*
import java.util.Arrays
import kotlin.math.*
import kotlin.random.Random

typealias Pos = DPos2
class ColBoxTest {

    @Test
    fun rotate() {
    }

    @Test
    fun intersect() {
        var testNum = 0
        // test if two same colBoxes can intersect
        println("t${++testNum}")
        var box1 = ColBox(Pos.ORIGIN, DDim2(1.0, 1.0))
        var box2 = ColBox(Pos.ORIGIN, DDim2(1.0, 1.0))
        assertTrue(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // test if two colBoxes with different size can intersect
        println("t${++testNum}")
        box1 = ColBox(Pos.ORIGIN, DDim2(2.0, 2.0))
        box2 = ColBox(Pos.ORIGIN, DDim2(1.0, 1.0))
        assertTrue(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // test two boxes touching
        println("t${++testNum}")
        box1 = ColBox(Pos.ORIGIN, DDim2(1.0, 1.0))
        box2 = ColBox(Pos.UP * 1.0, DDim2(1.0, 1.0))
        assertTrue(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // move the second box up, they should not touch this time
        println("t${++testNum}")
        box2 += Pos.UP
        assertFalse(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // test the case when on box enclose the other
        println("t${++testNum}")
        box1 = ColBox(Pos.ORIGIN, DDim2(3.0, 3.0))
        box2 = ColBox(Pos(1.5, 1.5), DDim2(1.0, 1.0))
        assertTrue(box1.intersect(box2))
        assertTrue(box2.intersect(box1))
        println("pass")

        // test polygon other than rectangle, first test right triangle
        println("t${++testNum}")
        box1 = ColBox(arrayOf(Pos.ORIGIN, Pos.RT, Pos.UP))
        box2 = ColBox(arrayOf(Pos.ORIGIN, Pos.RT, Pos.UP))
        assertTrue(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // move the second box a little bit, they should touch this time
        println("t${++testNum}")
        box2 += Pos.UP * 0.5
        assertTrue(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // move it further, they should not touch this time
        println("t${++testNum}")
        box2 += Pos.UP
        assertFalse(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // generate two polygons with PT_CNT points randomly, they share one point, so they should intersect
        println("t${++testNum}")
        val PT_CNT = 5000
        val samePt = Pos(1.0, 1.0)
        val pts1 = Array(PT_CNT){Pos.randUnit() * 1e5}
        val pts2 = Array(PT_CNT){Pos.randUnit() * 1e5}
        pts1[PT_CNT - 1] = samePt
        pts2[PT_CNT - 1] = samePt
        var startTm = System.currentTimeMillis()
        box1 = genRandPolygon(pts1)
        box2 = genRandPolygon(pts2)
        println("time to construct two boxes: ${System.currentTimeMillis() - startTm} ms")
        startTm = System.currentTimeMillis()
        assertTrue(box1.intersect(box2))
        println("time to test intersection: ${System.currentTimeMillis() - startTm} ms")
        println("pass")
    }

    /**
     * @param pts the points of the polygon, the first three should properly form a triangle
     */
    fun genRandPolygon(pts : Array<DPos2>) : ColBox {
        // sort the points using angle with horizontal line
        val sortedPts = pts.copyOf()
        val avePt : DPos2 = sortedPts.reduce { acc, dPos2 -> acc + dPos2 } / sortedPts.size.toDouble()
        Arrays.sort(sortedPts, 0, sortedPts.size) { o1, o2 ->
            var to1 = o1 - avePt
            var to2 = o2 - avePt
            val ang1 = atan2(to1.y, to1.x)
            val ang2 = atan2(to2.y, to2.x)
            // first sort by angle, then by distance (radius)
            if (abs(ang1 - ang2) > DOUBLE_PRECISION){
                if (ang1 < ang2) -1 else 1
            } else {
                if (to1.len() < to2.len()) -1 else 1
            }
        }
        return ColBox(sortedPts)
    }

    @Test
    fun testGenRandPolygon(){
        // generate random points, print them in the format of x, y , no parentheses
        val pts = Array(100){(DPos2.randUnit() * Random.nextDouble() * 100.0)}
        val box = genRandPolygon(pts)
        for (pt : DPos2 in box.pts) {
            println("${pt.x}, ${pt.y}")
        }
        // print the original point order
        println("original order:")
        for (pt : DPos2 in pts) {
            println("${pt.x}, ${pt.y}")
        }

    }
}