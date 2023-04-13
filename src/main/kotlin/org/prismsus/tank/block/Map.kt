package org.prismsus.tank.block

/**
 * The game map containing all blocks.
 */
class Map(var blocks: Array<Array<Int>>) {
    fun getBlock(x: Int, y: Int): Block {
        return Block.fromInt(x, y, blocks[x][y])
    }
}