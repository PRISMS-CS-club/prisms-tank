package org.prismsus.tank.elements

import org.prismsus.tank.utils.INIT_BULLET_COLBOX
import org.prismsus.tank.utils.INIT_BULLET_SPEED
import org.prismsus.tank.utils.collidable.ColRect

class Bullet(uid: Long, var speed: Double = INIT_BULLET_SPEED, override val colPoly: ColRect = INIT_BULLET_COLBOX.copy()) :
    MovableElement(uid, -1, colPoly) {
    constructor(uid: Long, props: BulletProps) : this(uid, props.speed, props.colBox)

    override val serialName: String
        get() = "Blt"
    var damage: Int = -1

}

class BulletProps(val speed: Double = INIT_BULLET_SPEED, val colBox: ColRect = INIT_BULLET_COLBOX){
    fun copy() : BulletProps {
        return BulletProps(speed, colBox.copy())
    }
}