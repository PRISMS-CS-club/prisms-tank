package org.prismsus.tank.elements

import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.DVec2

abstract class MovableElement(uid: Long, hp: Int, colPoly: ColPoly) :
    GameElement(uid, hp, colPoly), TimeUpdatable {

    var curVelo: DVec2 = DVec2(.0, .0)
    var curAngV: Double = .0

    override fun updateByTime(dt: Long) {
        colPoly += curVelo * dt.toDouble()
        // center is the intersection of two diagonals
        colPoly.rotateAssign(curAngV * dt.toDouble())
    }

}