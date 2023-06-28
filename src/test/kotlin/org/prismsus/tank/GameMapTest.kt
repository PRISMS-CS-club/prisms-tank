package org.prismsus.tank

import org.junit.jupiter.api.Test
import org.prismsus.tank.elements.GameMap
import org.junit.jupiter.api.Assertions.*
import org.prismsus.tank.elements.Block
import org.prismsus.tank.elements.BreakableBlock
import org.prismsus.tank.elements.SolidBlock
import org.prismsus.tank.utils.CoordPanel
import org.prismsus.tank.utils.IDim2
import org.prismsus.tank.utils.IPos2

class GameMapTest{
    @Test
    fun constructByFile(){
        run {
            val filePath = "testing.json"
            val gameMap = GameMap(filePath)
            assertTrue(gameMap.width == 3 && gameMap.height == 3)
            /*
            "SldBlk", "", "",
            "SldBlk", "", "",
            "BrkBlk", "", ""
        * */
            val targetMap: Array<Array<Block?>> = Array(3) { Array(3) { null } }
            targetMap[0][0] = BreakableBlock(0, IPos2(0, 0))
            targetMap[0][1] = SolidBlock(1, IPos2(0, 1))
            targetMap[0][2] = SolidBlock(2, IPos2(0, 2))
            assertTrue(gameMap.blocks contentDeepEquals targetMap)
        }

        run{
            val filePath = "default.json"
            val gameMap = GameMap(filePath)
            // use coordPanel to display it
            val height = gameMap.height
            val width = gameMap.width
            val Cpanel = CoordPanel(IDim2(1, 1), IDim2(50, 50))
            for (i in 0 until height){
                for (j in 0 until width){
                    val block = gameMap.blocks[i][j]
                    println("block[$i][$j] pos= ${block?.colPoly?.bottomLeftPt} ")
                    if (block != null){
                        Cpanel.drawCollidable(block.colPoly)
                    }
                }
            }
            Cpanel.showFrame()
            val quadPanel = gameMap.quadTree.getCoordPanel(IDim2(1000, 1000))
            quadPanel.showFrame()
            while(true){}
        }
    }
}