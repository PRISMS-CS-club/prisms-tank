package org.prismsus.tank

import org.junit.jupiter.api.Test
import org.prismsus.tank.utils.*
import org.junit.jupiter.api.Assertions.*
import org.prismsus.tank.utils.collidable.*
import java.awt.Color
import java.awt.Graphics2D
import kotlin.random.Random

typealias Pos = DPos2

class ColPolyTest {

    @Test
    fun rotate() {
        var testNum = 0
        // create a square box, rotate it by 90 degree, it should have the same point set as the original
        println("t${++testNum}")
        var box1 : ColPoly = ColRect.byTopLeft(Pos(0.0, 1.0), DDim2(1.0, 1.0))
        var box2 : ColPoly = ColRect.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0)).rotateDeg(90.0, Pos.ORIGIN) as ColPoly


        assertTrue(box2 equalPtSet box1)
        println("passed")

        // create a square box, rotate it by 180 degree from the center, it should have the same point set as the original
        println("t${++testNum}")
        box1 = ColRect.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0))
        box2 = ColRect.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0)).rotateDeg(180.0, Pos(.5, -.5)) as ColPoly
        assertTrue(box2 == box1)
        println("passed")

        // create a rectangle box with longer side on x axis, rotate it by 90 degree, it should have the same point set
        // with a rectangle box with longer side on y axis
        println("t${++testNum}")
        box1 = ColRect.byTopLeft(Pos(0.0, 1.0), DDim2(2.0, 1.0)).rotateDeg(90.0, Pos.ORIGIN) as ColPoly
        box2 = ColRect.byTopLeft(Pos(-1.0, 2.0), DDim2(1.0, 2.0))
        assertTrue(box2 == box1)
        println("passed")

        // create a rectangle box with longer side on x axis, rotate it by -90 degree, it should have the same point set
        // with a rectangle box with longer side on y axis
        println("t${++testNum}")
        box1 = ColRect.byTopLeft(Pos(0.0, 1.0), DDim2(2.0, 1.0)).rotateDeg(-90.0, Pos.ORIGIN) as ColPoly
        box2 = ColRect.byTopLeft(Pos(0.0, 0.0), DDim2(1.0, 2.0))
        assertTrue(box2 == box1)
        println("passed")
    }

    @Test
    fun intersect() {
        var testNum = 0
        // test if two same ColPoly.byTopLeftes can intersect
        println("t${++testNum}")
        var box1 : Collidable = ColRect.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0))
        var box2 : Collidable = ColRect.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0))
        assertTrue(box1.collide(box2) && box2.collide(box1))
        println("pass")

        // test if two ColPoly.byTopLeftes with different size can intersect
        println("t${++testNum}")
        box1 = ColRect.byTopLeft(Pos.ORIGIN, DDim2(2.0, 2.0))
        box2 = ColRect.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0))
        assertTrue(box1.collide(box2) && box2.collide(box1))
        println("pass")

        // test two boxes touching
        println("t${++testNum}")
        box1 = ColRect.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0))
        box2 = ColRect.byTopLeft(Pos.UP, DDim2(1.0, 1.0))
        assertTrue(box1.collide(box2) && box2.collide(box1))
        println("pass")

        // move the second box up, they should not touch this time
        println("t${++testNum}")
        box2 = box2 + DVec2.UP
        assertFalse(box1.collide(box2) && box2.collide(box1))
        println("pass")

        // test the case when on box enclose the other
        println("t${++testNum}")
        box1 = ColRect.byTopLeft(Pos.ORIGIN, DDim2(3.0, 3.0))
        box2 = ColRect.byTopLeft(Pos(0.0, -1.5), DDim2(1.0, 1.0))
        assertTrue(box1.collide(box2))
        assertTrue(box2.collide(box1))
        print(box1.collidePts(box2).contentToString())
        println("pass")

        // test polygon other than rectangle, first test right triangle
        println("t${++testNum}")
        box1 = ColPoly(arrayOf(Pos.ORIGIN, Pos.RT, Pos.UP))
        box2 = ColPoly(arrayOf(Pos.ORIGIN, Pos.RT, Pos.UP))
        assertTrue(box1.collide(box2) && box2.collide(box1))
        println("pass")

        // move the second box a little bit, they should touch this time
        println("t${++testNum}")
        box2 = box2 + DVec2.UP * 0.5
        assertTrue(box1.collide(box2) && box2.collide(box1))
        println("pass")

        // move it further, they should not touch this time
        println("t${++testNum}")
        box2 = box2 + DVec2.UP
        assertFalse(box1.collide(box2) && box2.collide(box1))
        println("pass")

        // generate two polygons with PT_CNT points randomly, they share one point, so they should intersect
        println("t${++testNum}")
        val PT_CNT = 5000
        val samePt = Pos(1.0, 1.0)
        val pts1 = Array(PT_CNT){(DVec2.randUnitVec() * Random.nextDouble(1.0, 15.0)).toPt()}
        val pts2 = Array(PT_CNT){(DVec2.randUnitVec() * Random.nextDouble(1.0, 15.0)).toPt()}
        pts1[PT_CNT - 1] = samePt
        pts2[PT_CNT - 1] = samePt
        var startTm = System.currentTimeMillis()
        box1 = ColPoly.byUnorderedPtSet(pts1)
        box2 = ColPoly.byUnorderedPtSet(pts2)
        println("time to construct two boxes: ${System.currentTimeMillis() - startTm} ms")
        startTm = System.currentTimeMillis()
        assertTrue(box1.collide(box2))
        println("time to test intersection: ${System.currentTimeMillis() - startTm} ms")
        println("pass")
        val panel = CoordPanel(IDim2(1, 1), IDim2(15, 15))
        panel.drawCollidable(box1)
        panel.graphicsModifier = { g : Graphics2D ->
            g.color = Color.RED
        }
        panel.drawCollidable(box2)
        panel.showFrame()
        while(true){}
    }


    @Test
    fun testGenRandPolygon(){
        // generate random points, print them in the format of x, y , no parentheses
        val pts = Array(100){(DVec2.randUnitVec() * Random.nextDouble() * 100.0).toPt()}
        val box = ColPoly.byUnorderedPtSet(pts)
        for (pt : DPos2 in box.pts) {
            println("${pt.x}, ${pt.y}")
        }
        // print the original point order
        println("original order:")
        for (pt : DPos2 in pts) {
            println("${pt.x}, ${pt.y}")
        }

    }


    @Test
    fun union(){
        // create a rectangle and a triangle
        // one vertex of the triangle is inside the rectangle
        // the other two vertices are outside the rectangle
        run {
            return@run
            val rect = ColRect.byTopLeft(DPos2(-.5, .5), DDim2(1.0, 1.0))
            val tri = ColPoly(arrayOf(DPos2(0.0, 0.0), DPos2(2.0, 0.0), DPos2(1.0, 1.0)))
            val panel = CoordPanel(IDim2(1, 1), IDim2(80, 80))
            panel.drawCollidable(rect, tri)
            panel.showFrame()


            val panel2 = CoordPanel(IDim2(1, 1), IDim2(80, 80))

            print((rect collidePts tri).contentToString())
            val union = rect.union(tri)!!
            panel2.drawCollidable(union)
            panel2.showFrame()
        }

        run{
            return@run
            // create a large triangle in the center, and a rectangle that pass through the center
            val tri = ColPoly(arrayOf(DPos2(-1.0, -1.0), DPos2(1.0, -1.0), DPos2(0.0, 1.0)))
            val rect = ColRect(DPos2.ORIGIN, DDim2(4.0, .5))
            val sepP = CoordPanel(IDim2(1, 1), IDim2(80, 80))
            sepP.drawCollidable(tri)
            sepP.graphicsModifier = { g : Graphics2D ->
                g.color = Color.RED
            }
            sepP.drawCollidable(rect)
            sepP.showFrame()
            val union = rect.union(tri)!!
            val unionP = CoordPanel(IDim2(1, 1), IDim2(80, 80))
            unionP.drawCollidable(union)
            unionP.showFrame()
        }

        run{
            return@run
            val tri = ColPoly(arrayOf(
                DPos2(.0, 1.0),
                DPos2(3.0, 0.0),
                DPos2(0.0, -1.0)
            ))

            val rect = ColRect(DPos2.ORIGIN, DDim2(4.0, .5))
            val sepP = CoordPanel(IDim2(1, 1), IDim2(150, 150))
            sepP.drawCollidable(tri)
            sepP.graphicsModifier = { g : Graphics2D ->
                g.color = Color.RED
            }
            sepP.drawCollidable(rect)
            sepP.showFrame()
            val union = rect.union(tri)!!
            val unionP = CoordPanel(IDim2(1, 1), IDim2(150, 150))
            unionP.drawCollidable(union)
            unionP.showFrame()
            println(union)
        }

        run{
            return@run
            val separateP = CoordPanel(IDim2(1, 1), IDim2(150, 150))
            // create one random polygon at the center and one rectangle that pass through the center
            // poly=pts=[(-1.4303593576413736, -0.6660861740378866), (-0.927681268874418, -0.5210836385154236), (0.6451508438052175, -1.9505910001882056), (-0.037410982015603104, -0.016354731672313102), (0.1641716240187499, 0.19371063626176493), (0.608525867996999, 0.70937170825132), (0.26603689809620157, 2.35340069805903), (-0.8669666772386945, 0.9582756873894713), (-1.1489041684482149, 1.0988962689665125), (-1.3610651218633572, 0.6199913535366662)]
            // create poly arr from the above string, full precision
            val polyArr = arrayOf(
                DPos2(-1.4303593576413736, -0.6660861740378866),
                DPos2(-0.927681268874418, -0.5210836385154236),
                DPos2(0.6451508438052175, -1.9505910001882056),
                DPos2(-0.037410982015603104, -0.016354731672313102),
                DPos2(0.1641716240187499, 0.19371063626176493),
                DPos2(0.608525867996999, 0.70937170825132),
                DPos2(0.26603689809620157, 2.35340069805903),
                DPos2(-0.8669666772386945, 0.9582756873894713),
                DPos2(-1.1489041684482149, 1.0988962689665125),
                DPos2(-1.3610651218633572, 0.6199913535366662)
            )
            val randArr = Array(150) { (DVec2.randUnitVec() * Random.nextDouble() * 3.0).toPt() }
            val poly = ColPoly.byUnorderedPtSet(randArr)
//            val poly = ColPoly(polyArr)
            val rect = ColRect(DPos2.ORIGIN, DDim2(4.0, 2.0))
            println("poly=$poly")
            separateP.drawCollidable(poly)
            separateP.graphicsModifier = { g : Graphics2D ->
                g.color = Color.RED
            }
            separateP.drawCollidable(rect)
            separateP.showFrame()
            val union = poly.union(rect)!!
            println("union=$union")
            val unionP = CoordPanel(IDim2(1, 1), IDim2(150, 150))
            unionP.drawCollidable(union)
            unionP.showFrame()
        }

        run{
            // create two random polygons
            val poly1 = ColPoly.byUnorderedPtSet(Array(120) { (DVec2.randUnitVec() * Random.nextDouble() * 3.0).toPt() })
            val poly2 = ColPoly.byUnorderedPtSet(Array(120) { (DVec2.randUnitVec() * Random.nextDouble() * 3.0).toPt() })
            val sepP = CoordPanel(IDim2(1, 1), IDim2(150, 150))
            sepP.drawCollidable(poly1)
            sepP.graphicsModifier = { g : Graphics2D ->
                g.color = Color.RED
            }
            sepP.drawCollidable(poly2)
            sepP.showFrame()
            val union = poly1.union(poly2)!!
            val unionP = CoordPanel(IDim2(1, 1), IDim2(150, 150))
            unionP.drawCollidable(union)
            unionP.showFrame()
        }


        while(true){
        }
    }

    @Test
    fun angleRotated(){

        // rotate 100 times by random center
        for (i in 0 until 100) {
            val randObj = Line(DPos2(0.0, .0), DPos2(.0, 1.0))
            val center = DVec2.randUnitVec()
            var rot = Random.nextDouble() * 360.0
            randObj.rotateAssignDeg(rot, center.toPt())

            if (rot < 0)
                rot += 360.0

            assertEquals(rot, randObj.angleRotated.toDeg(), DOUBLE_PRECISION)
        }

    }

}