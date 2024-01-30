package org.prismsus.tank.game

interface ControllerRequestTypes {
    val serialName : String
        get() = ""
}

/**
 * For controllers to gain properties of the tank and weapon
 */
enum class TankWeaponInfo : ControllerRequestTypes {
    // Tank related
    TANK_HP,
    TANK_MAX_HP,
    TANK_LTRACK_SPEED,
    TANK_RTRACK_SPEED,
    TANK_TRACK_MAX_SPEED,
    TANK_COLBOX,
    TANK_POS,
    TANK_ANGLE,
    TANK_VIS_RANGE,

    // weapon related
    WEAPON_RELOAD_RATE_PER_SEC,
    WEAPON_MAX_CAPACITY,
    WEAPON_CUR_CAPACITY,
    WEAPON_DAMAGE,
    WEAPON_COLBOX,
    COMBINED_COLBOX,
    BULLET_COLBOX,
    BULLET_SPEED
}

enum class OtherRequests : ControllerRequestTypes{
    GET_VISIBLE_ELEMENTS,
    GET_VISIBLE_TANKS,
    GET_VISIBLE_BULLETS,
    GET_VISITED_ELEMENTS,
    CHECK_BLOCK_AT,
    CHECK_COLLIDING_GAME_ELES,
    SET_DEBUG_STRING,
    GET_HP_MONEY_INC_RATE,
    FIRE{
        override val serialName: String = "fire" },
    SET_LTRACK_SPEED{
        override val serialName: String = "lTrack" },
    SET_RTRACK_SPEED{
        override val serialName: String = "rTrack" },
}