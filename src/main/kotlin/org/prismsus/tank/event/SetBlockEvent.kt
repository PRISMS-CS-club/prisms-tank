package org.prismsus.tank.event

/**
 * Set a block at a specific location.
 * @property x The x coordinate of the block.
 * @property y The y coordinate of the block.
 * @property block The ID of block to set.
 */
class SetBlockEvent(val x: Int, val y: Int, val block: Int) : Event() {
}