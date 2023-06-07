package org.prismsus.tank

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.prismsus.tank.utils.*

import org.prismsus.tank.utils.collidable.ColAARect
import org.prismsus.tank.utils.collidable.ColTreeSet
import org.prismsus.tank.utils.collidable.DPos2
import kotlin.random.Random

class ColTreeSetTest {

    @Test
    fun Shapes() {
        // first randomly generate DPos2
        val randPts = Array(200) { DVec2.randUnitVec() * Random.nextDouble(-5.0, 5.0) }
        println(randPts.contentToString())
        val quadTree = ColTreeSet(0, ColAARect(DPos2(0, 0), DDim2(10, 10)))
        for (pt in randPts) {
            quadTree.insert(pt.toPt())
        }

        assertEquals(randPts.size, quadTree.allSubCols.size)

        val panel = CoordPanel(IDim2(1, 1), IDim2(50, 50))

        for (col in quadTree.allSubCols){
            panel.drawCollidable(col)
            assertTrue(randPts.contains((col as DPos2).toVec()))
        }

        for (line in quadTree.allSubPartitionLines){
            panel.drawCollidable(line)
        }

        panel.showFrame()
        while(true){}
    }

    @Test
    fun getCordPanel(){
        var randPts = Array(200) { DVec2.randUnitVec() * Random.nextDouble(-5.0, 5.0) }
        val quadTree = ColTreeSet(0, ColAARect(DPos2(0, 0), DDim2(10, 10)))
        for (pt in randPts) {
            quadTree.insert(pt.toPt())
        }
        val panel = quadTree.getCoordPanel(IDim2(1000, 1000))
        panel.showFrame()
        while(true){}
    }
}