package org.prismsus.tank.element

import org.prismsus.tank.utils.collidable.ColBox
import org.prismsus.tank.utils.DVec2

abstract class MovableElement(uid: Long, hp: Int, colBox: ColBox) :
    GameElement(uid, hp, colBox) {

    var curVelo: DVec2 = DVec2(.0, .0)
    var curAngV: Double = .0

    open fun updatePosByTime(dt: Long) {
        colBox += curVelo * dt.toDouble()
        // center is the intersection of two diagonals
        colBox.rotateAssign(curAngV * dt.toDouble())
    }

}