package org.prismsus.tank.elements

import org.prismsus.tank.utils.collidable.ColBox
import org.prismsus.tank.utils.DVec2

abstract class MovableElement(uid: Long, hp: Int, colBox: ColBox) :
    GameElement(uid, hp, colBox), TimeUpdatable {

    var curVelo: DVec2 = DVec2(.0, .0)
    var curAngV: Double = .0

    override fun updateByTime(dt: Long) {
        colBox += curVelo * dt.toDouble()
        // center is the intersection of two diagonals
        colBox.rotateAssign(curAngV * dt.toDouble())
    }

}