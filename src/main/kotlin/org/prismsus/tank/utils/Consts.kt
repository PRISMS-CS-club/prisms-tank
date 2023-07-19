package org.prismsus.tank.utils

import  org.prismsus.tank.elements.*
import org.prismsus.tank.utils.collidable.DPos2
import org.prismsus.tank.utils.collidable.ColRect
import kotlin.math.round

const val DOUBLE_PRECISION: Double = 1e-8

// constants for game configuration and default values
const val DEF_BLOCK_HP: Int = 50
const val INIT_TANK_HP: Int = 150
const val INIT_TANK_TRACK_SPEED: Double = 3.0
const val INIT_TANK_VIS_RANGE : Double = 4.0
val INIT_TANK_COLBOX: ColRect
    get() = ColRect(DPos2(.0, .0), DDim2(.6, .6))
val DEF_BLOCK_COLBOX: ColRect
    get() = ColRect.byTopLeft(DPos2(.0, 1.0), DDim2(1.0 - DOUBLE_PRECISION, 1 - DOUBLE_PRECISION))
val INIT_RECT_WEAPON_COLBOX
    get() = ColRect(DPos2(.0, .0), DDim2(.2, .4))

// this value can be modified by shifting the colPoly by the position of the block
// default value is the colPoly for Block at (0,0) in the map
val INIT_BULLET_SPEED: Double = 8.0
val INIT_BULLET_COLBOX: ColRect
    get() = ColRect(DPos2(.0, .0), DDim2(.08, .08))
val INIT_BULLET_PROP: BulletProps
    get() = BulletProps(INIT_BULLET_SPEED, INIT_BULLET_COLBOX)
val INIT_RECT_WEAPON_RPOPS
    get() = RectWeaponProps(
    10,
    300,
    20,
    3.0,
    INIT_BULLET_PROP,
    INIT_RECT_WEAPON_COLBOX,
)


val ELE_SERIAL_NAME_TO_CLASS
    get() = mapOf(
    "BrkBlk" to BreakableBlock::class,
    "SldBlk" to SolidBlock::class,
) // element's serial name to its class

const val DEF_TICK_RATE : Int = 128  // frame rate of the game
val DEF_MS_PER_LOOP : Long = round(1000.0 / DEF_TICK_RATE).toLong()

const val DEF_DEBUG_TICK_RATE : Int = 24  // frame rate of the debug panel
val DEF_DEBUG_MS_PER_LOOP: Long = round(1000.0 / DEF_TICK_RATE).toLong()

const val EVT_NUM_DIGIT : Int = 3 // for any floating points transmitted by event, the number of digits after decimal point