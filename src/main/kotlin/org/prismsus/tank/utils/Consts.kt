package org.prismsus.tank.utils
import  org.prismsus.tank.element.*
import org.prismsus.tank.utils.collidable.DPos2
import org.prismsus.tank.utils.collidable.RectColBox

// constants for game configuration and default values

val DEF_BLOCK_HP : Int = 20
val DEF_TANK_HP : Int = 100
val DEF_TANK_TRACK_SPEED : Double = 5.0


val DEF_BLOCK_COLBOX : RectColBox = RectColBox.byTopLeft(DPos2(.0,1.0), DDim2(1.0,1.0))
// default value is the colBox for Block at (0,0) in the map
// this value can be modified by shifting the colBox by the position of the block
val DEF_TANK_COLBOX : RectColBox = RectColBox(DPos2(.0,1.0), DDim2(.8,.8))
val DEF_BULLET_COLBOX : RectColBox = RectColBox(DPos2(.0,1.0), DDim2(.2,.2))


const val DOUBLE_PRECISION : Double = 1e-8

val ELE_SERIAL_NAME_TO_CLASS = mapOf(
    "BrkBlk" to BreakableBlock::class,
    "SldBlk" to SolidBlock::class,
) // element's serial name to its class