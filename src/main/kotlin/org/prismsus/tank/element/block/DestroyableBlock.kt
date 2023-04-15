package org.prismsus.tank.element.block

import org.prismsus.tank.utils.IVec2

class DestroyableBlock(uid : Long, pos : IVec2) : Block(uid, pos, BLOCK_HP){
    companion object {
        const val BLOCK_HP : Int = 20
    }
}