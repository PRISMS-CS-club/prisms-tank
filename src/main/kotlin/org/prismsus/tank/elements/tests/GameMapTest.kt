package org.prismsus.tank.elements.tests

import org.junit.jupiter.api.Test
import org.prismsus.tank.elements.GameMap
import org.junit.jupiter.api.Assertions.*
import org.prismsus.tank.elements.Block
import org.prismsus.tank.elements.BreakableBlock
import org.prismsus.tank.elements.SolidBlock
import org.prismsus.tank.utils.IPos2

class GameMapTest{
    @Test
    fun constructByFile(){
        val filePath = "testing.json"
        val gameMap = GameMap(filePath)
        assertTrue(gameMap.width == 3 && gameMap.height == 3)
        /*
            "SldBlk", "", "",
            "SldBlk", "", "",
            "BrkBlk", "", ""
        * */
        val targetMap : Array<Array<Block?>> = Array(3){Array(3){null}}
        targetMap[0][0] = BreakableBlock(0, IPos2(0, 0))
        targetMap[0][1] = SolidBlock(1, IPos2(0, 1))
        targetMap[0][2] = SolidBlock(2, IPos2(0, 2))
        assertTrue(gameMap.blocks contentDeepEquals targetMap)
    }
}