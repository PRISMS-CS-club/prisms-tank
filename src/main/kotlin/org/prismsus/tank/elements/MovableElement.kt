package org.prismsus.tank.elements

import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.DVec2

abstract class MovableElement(uid: Long, hp: Int, colPoly: ColPoly) :
    GameElement(uid, hp, colPoly), TimeUpdatable {

    constructor(other : MovableElement) : this(other.uid, other.hp, other.colPoly) {
        velocity = other.velocity
        angVelocity = other.angVelocity
    }
    var velocity: DVec2 = DVec2(.0, .0)
    var angVelocity: Double = .0

    override fun updateByTime(dt: Long) {
        colPoly += velocity * dt.toDouble()
        // center is the intersection of two diagonals
        colPoly.rotateAssign(angVelocity * dt.toDouble())
    }
    abstract fun colPolyAfterMove(dt : Long) : ColPoly
    abstract fun willMove(dt : Long) : Boolean

}