package org.prismsus.tank.elements

import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.collidable.ColRect

/**
 * @param uid unique id of the block
 * @param pos position of the block in the game map (in block unit). Notice that this is not the
 * coordinate, but the discrete position in the map. (for example a map have 10x10 blocks, then
 * the position of the block in the map is integer, (0,0) to (9,9))
 *
 * Gives all relevant data of a block in the game map.
 */
abstract class Block(uid : Long, val pos : IVec2, hp : Int = -1, colBox: ColRect = DEF_BLOCK_COLBOX)
    : GameElement(uid, hp, colBox){
    init{
        this.colPoly.rotationCenter = pos.toDVec2().toPt()
        // the accepted parameter is the position in the map
        // we want to make sure that the colPoly is in top-left position
    }

    /**
     * test if two blocks have structural equality, does not test their uid
     * if you want to test their uid, use [strictEqual]
     * @param other the other block
     * @return whether the two blocks are structurally equal
     * @see strictEqual for strict equality with uid
    * */
    override fun equals(other: Any?): Boolean {
        if (other !is Block) return false
        return pos == other.pos && hp == other.hp && colPoly.equalPtSet(other.colPoly)
    }

    /**
     * test if two blocks have structural equality, including their uid
     * @param other the other block
     * @return whether the two blocks are structurally equal
     * @see equals for structural equality without uid
    * */
    infix fun strictEqual(other: Any?): Boolean {
        if (other !is Block) return false
        return pos == other.pos && hp == other.hp && colPoly.equalPtSet(other.colPoly) && uid == other.uid
    }

    override fun toString(): String {
        return "uid=$uid, pos=$pos, hp=$hp, colPoly=$colPoly"
    }
}

class BreakableBlock(uid : Long, pos : IVec2) : Block(uid, pos, DEF_BLOCK_HP){
    override val serialName: String
        get() = "BrkBlk"
}

class SolidBlock(uid : Long, pos : IVec2) : Block(uid, pos){
    override val serialName: String
        get() = "SldBlk"
}