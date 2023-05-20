package org.prismsus.tank.element

import org.prismsus.tank.game.Game
import org.prismsus.tank.utils.DOUBLE_PRECISION
import org.prismsus.tank.utils.DVec2
import org.prismsus.tank.utils.collidable.ColBox
import org.prismsus.tank.utils.collidable.DPos2
import org.prismsus.tank.utils.collidable.RectColBox
import org.prismsus.tank.utils.nextUid

open class Weapon(
    val damage : Int,
    val bulletProps : BulletProps,
    override val colBox: ColBox,
    override var belongTo: GameElement,
    override var centerOffset: DVec2,
    val firingPos : DPos2,
) : SubGameElement {
    override val serialName: String
        get() = "Wep"
    fun fire() : Bullet {
        val bullet = Bullet(nextUid, bulletProps)
        bullet.colBox.rotateTo(belongTo.colBox.angleRotated)
        bullet.colBox.bottomMidPt = firingPos
        bullet.curVelo = DVec2.byPolar(belongTo.colBox.angleRotated, bulletProps.speed)
        bullet.damage = damage
        return bullet
    }
}

class rectWeapon(
    damage: Int,
    bulletProps: BulletProps,
    colBox: RectColBox,
    belongTo: GameElement,
    centerOffset: DVec2 = DVec2(.0, belongTo.colBox.height / 2.0),
    firingPos : DPos2 =  colBox.topMidPt + centerOffset + DVec2(0.0,  DOUBLE_PRECISION * 100)
) : Weapon(damage, bulletProps, colBox, belongTo, centerOffset, firingPos)
{
}
