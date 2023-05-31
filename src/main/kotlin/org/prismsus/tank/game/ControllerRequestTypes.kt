package org.prismsus.tank.game

interface ControllerRequestTypes {
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
    GET_VISITED_ELEMENTS,
    SHOOT,
    SET_LTRACK_SPEED,
    SET_RTRACK_SPEED,
}