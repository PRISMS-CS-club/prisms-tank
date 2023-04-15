package org.prismsus.tank.element.block

import org.prismsus.tank.element.GameElement
import org.prismsus.tank.utils.ColBox
import org.prismsus.tank.utils.DEF_BLOCK_COLBOX
import org.prismsus.tank.utils.IVec2

/**
 * Gives all relevant data of a block in the game map.
 *
 * Note: the `Block` class is only for passing block data between game and bot class. To improve
 * game performance, the actual blocks should be stored as an integer array in the game map.
 */
abstract class Block(uid : Long, val pos : IVec2, hp : Int = -1, colBox: ColBox = DEF_BLOCK_COLBOX)
    : GameElement(uid, hp, colBox){
    init{
        this.colBox = (colBox + pos.toDvec2()) as ColBox
    }
}