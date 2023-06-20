package org.prismsus.tank.elements

import org.prismsus.tank.utils.INIT_BULLET_COLBOX
import org.prismsus.tank.utils.INIT_BULLET_SPEED
import org.prismsus.tank.utils.collidable.ColRect
import org.prismsus.tank.utils.errNE

class Bullet(uid: Long, var speed: Double = INIT_BULLET_SPEED, override val colPoly: ColRect = INIT_BULLET_COLBOX.copy()) :
    MovableElement(uid, -1, colPoly) {
    constructor(uid: Long, props: BulletProps) : this(uid, props.speed, props.colBox)
    // need to convert from m/s to m/ms

    override val serialName: String
        get() = "Blt"
    var damage: Int = -1
    override fun willMove(dt: Long): Boolean {
        return speed errNE .0
    }

    override fun colPolyAfterMove(dt: Long): ColRect {
        return colPoly.copy().apply { this += curVelo * dt.toDouble() }
    }

    override infix fun processCollision(other: GameElement): Boolean {
        removeStat = RemoveStat.TO_REMOVE
        return super.processCollision(other)
    }

    override fun updateByTime(dt: Long) {
        super.updateByTime(dt)
    }
 }

class BulletProps(val speed: Double = INIT_BULLET_SPEED, val colBox: ColRect = INIT_BULLET_COLBOX){
    fun copy() : BulletProps {
        return BulletProps(speed, colBox.copy())
    }
}