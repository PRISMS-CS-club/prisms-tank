package org.prismsus.tank.markets

import org.prismsus.tank.utils.*

data class UpgradeEntry<T : Comparable<T>>(
    val type: UpgradeType,
    val incrementRg: ClosedRange<T>,
    val valRg: ClosedRange<T>,
    val probability: Double
) {
    enum class UpgradeType {
        MONEY{
            override val serialName = "money"
            override val defaultValue = INIT_TANK_MONEY
            override val isIntValue = true
        },
        MAX_HP {
            override val serialName = "mHP"
            override val defaultValue = INIT_TANK_HP
            override val isIntValue = true
        },
        VIS_RADIUS {
            override val serialName = "visRad"
            override val defaultValue = INIT_TANK_VIS_RANGE
            override val isIntValue = false
        },
        TANK_BODY_AREA {
            override val serialName = "tkArea"
            override val defaultValue = 114514 // TODO
            override val isIntValue = false
        },
        TANK_BODY_EDGE_CNT {
            override val serialName = "tkEdgeCnt"
            override val defaultValue = 3
            override val isIntValue = false
        },
        TANK_SPEED {
            override val serialName = "tkSpd"
            override val defaultValue = INIT_TANK_TRACK_SPEED
            override val isIntValue = false
        },
        API_TOKEN_CNT {
            override val serialName = "APItoken"
            override val defaultValue = 114514 // TODO
            override val isIntValue = false
        },
        WEAPON_CAPACITY {
            override val serialName = "w.capa"
            override val defaultValue = INIT_RECT_WEAPON_RPOPS.maxCapacity
            override val isIntValue = false
        },
        WEAPON_DAMAGE {
            override val serialName = "w.dmg"
            override val defaultValue = INIT_RECT_WEAPON_RPOPS.damage
            override val isIntValue = false
        },
        WEAPON_LAUNCH_MIN_INTERV {
            override val serialName = "w.launchRt"
            override val defaultValue = INIT_RECT_WEAPON_RPOPS.damage
            override val isIntValue = false
        },
        WEAPON_RELOAD_RATE {
            override val serialName = "w.reload"
            override val defaultValue = INIT_RECT_WEAPON_RPOPS.reloadRate
            override val isIntValue = false
        },
        WEAPON_BULLET_SPEED {
            override val serialName = "w.bltSpd"
            override val defaultValue = INIT_BULLET_SPEED
            override val isIntValue = false
        },
        WEAPON_BULLET_WIDTH {
            override val serialName = "w.bltWid"
            override val defaultValue = INIT_BULLET_COLBOX.width
            override val isIntValue = false
        };

        abstract val serialName : String
        abstract val isIntValue : Boolean
        abstract val defaultValue : Number
    }
}

data class UpgradeRecord<T>(
    val type: UpgradeEntry.UpgradeType,
    val isInc: Boolean,
    val value: T,
    val cid: Long,
    val timeStamp: Long = game!!.elapsedGameMs
) {
}