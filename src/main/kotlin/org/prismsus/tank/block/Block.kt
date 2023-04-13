package org.prismsus.tank.block

import org.prismsus.tank.utils.IntVec2

/**
 * Gives all relevant data of a block in the game map.
 *
 * Note: the `Block` class is only for passing block data between game and bot class. To improve
 * game performance, the actual blocks should be stored as an integer array in the game map.
 */
abstract class Block(val x: Int, val y: Int) {
    val pos: IntVec2
        get() = IntVec2(x, y)

    /**
     * Whether the player can pass through this block.
     */
    abstract fun canPass(): Boolean

    /**
     * Defines how the bullet will act when it hits this block.
     * @property NONE The bullet will disappear.
     * @property DESTROY The bullet will disappear and the block will be destroyed.
     * @property PASS The bullet will pass through the block and the block will remain.
     * @property PASS_DESTROY The bullet will pass through the block and the block will be destroyed.
     */
    enum class BulletAction {
        NONE,
        DESTROY,
        PASS,
        PASS_DESTROY,
    }

    /**
     * The action the bullet will take when it hits this block.
     */
    abstract val bulletAction: BulletAction

    /**
     * Whether the player can see through this block.
     */
    abstract fun canSee(): Boolean

    companion object {
        /**
         * Creates a block from the given integer.
         */
        fun fromInt(x: Int, y: Int, block: Int): Block {
            TODO("To be implemented")
        }
    }
}