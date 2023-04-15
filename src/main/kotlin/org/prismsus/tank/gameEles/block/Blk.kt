package org.prismsus.tank.gameEles.block

import org.prismsus.tank.gameEles.GameEle
import org.prismsus.tank.utils.ColBox
import org.prismsus.tank.utils.DEF_BLK_COLBOX
import org.prismsus.tank.utils.Ivec2

/**
 * Gives all relevant data of a block in the game map.
 *
 * Note: the `Block` class is only for passing block data between game and bot class. To improve
 * game performance, the actual blocks should be stored as an integer array in the game map.
 */
abstract class Blk(uid : Long, val pos : Ivec2, hp : Int = -1, colBox: ColBox = DEF_BLK_COLBOX)
    : GameEle(uid, hp, colBox){
    init{
        this.colBox = (colBox + pos.toDvec2()) as ColBox
    }
}