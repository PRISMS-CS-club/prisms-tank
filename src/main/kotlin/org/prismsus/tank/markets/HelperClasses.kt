package org.prismsus.tank.markets

data class UpgradeEntry<T : Comparable<T>>(
    val type: UpgradeType,
    val incrementRg: ClosedRange<T>,
    val valRg: ClosedRange<T>,
    val probability: Double
) {
    enum class UpgradeType {
        MAX_HP {
            override val serialName = "mHP"
        },
        VIS_RADIUS {
            override val serialName = "visRad"
        },
        TANK_BODY_AREA {
            override val serialName = "tkArea"
        },
        TANK_BODY_EDGE_CNT {
            override val serialName = "tkEdgeCnt"
        },
        TANK_SPEED {
            override val serialName = "tkSpd"
        },
        API_TOKEN_CNT {
            override val serialName = "APItoken"
        },
        WEAPON_CAPACITY {
            override val serialName = "w.capa"
        },
        WEAPON_DAMAGE {
            override val serialName = "w.dmg"
        },
        WEAPON_LAUNCH_MIN_INTERV {
            override val serialName = "w.launchRate"
        },
        WEAPON_RELOAD_RATE {
            override val serialName = "w.reload"
        },
        WEAPON_BULLET_SPEED {
            override val serialName = "w.bltSpd"
        },
        WEAPON_BULLET_WIDTH {
            override val serialName = "w.bltWid"
        };

        abstract val serialName: String
    }
}

data class UpgradeRecord<T>(
    val type: UpgradeEntry.UpgradeType,
    val isInc: Boolean,
    val value: T,
    val cid: Long,
    val timeStamp: Long
) {
}