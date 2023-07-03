package org.prismsus.tank.elements

import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.collidable.ColRect
import org.prismsus.tank.utils.collidable.Line

// TODO: set the direction to launch bullet as one properties
open class Weapon(
    val damage : Int,
    val minInterv : Int,     // the minimum interval between two fires, in ms
    val maxCapacity : Int,   // the maximum capacity of bullet in the weapon
    val reloadRate : Double, // the rate of refilling bullet to its max capacity, per second
    val bulletProps : BulletProps,
    override val colPoly: ColPoly,
    override var belongTo: GameElement,
    override var offsetFromParentCenter: DVec2,
    val firingPosOffset : DVec2, // the relative position to fire the bullet, if y value is 1, it means that the bullet will be fired at the place with max y value
                           // and the x value of the rotation center of the weapon
) : SubGameElement, TimeUpdatable {
    override val serialName: String
        get() = "Wep"
    var curCapacity : Int = maxCapacity
    protected var lastFireTime : Long = 0

    open fun fire(bulletMovementAng : Double = belongTo.colPoly.angleRotated, bulletOrientationAng : Double = belongTo.colPoly.angleRotated) : Bullet?{
        throw NotImplementedError()
    }

    override fun updateByTime(dt: Long) {
        curCapacity += (reloadRate * dt / 1000).toInt()
    }
}

class RectWeapon(
    damage: Int,
    minInterv : Int,     // the minimum interval between two fires
    maxCapacity : Int,   // the maximum capacity of bullet in the weapon
    reloadRate : Double, // the rate of refilling bullet to its max capacity
    bulletProps: BulletProps,
    colBox: ColRect,
    belongTo: GameElement,
    offsetFromParentCenter: DVec2 = 1.0.toYvec(),
    firingPos : DVec2 = 1.0.toYvec()
) : Weapon(damage, minInterv, maxCapacity, reloadRate,  bulletProps, colBox, belongTo, offsetFromParentCenter, firingPos)
{
    override fun fire(bulletMovementAng : Double, bulletOrientationAng: Double): Bullet?
        {
            if (System.currentTimeMillis() - lastFireTime < minInterv) return null
            val bullet = Bullet(nextUid, bulletProps.copy())
            bullet.colPoly.rotateAssignTo(bulletOrientationAng)
            val weaponYLine = Line((colPoly as ColRect).rotationCenter, (colPoly).topMidPt)
            val weaponXLine = Line(colPoly.rotationCenter, (colPoly).rightMidPt)
            bullet.colPoly.bottomMidPt = colPoly.rotationCenter + weaponYLine.toVec() * firingPosOffset.y + weaponXLine.toVec() * firingPosOffset.x
            bullet.velocity = DVec2.byPolar(bulletProps.speed / 1000.0, bulletMovementAng)
            bullet.damage = damage
            lastFireTime = System.currentTimeMillis()
            curCapacity--
            return bullet
        }

}

open class WeaponProps(
    val damage: Int,
    val minInterv : Int,     // the minimum interval between two fires
    val maxCapacity : Int,   // the maximum capacity of bullet in the weapon
    val reloadRate : Double, // the rate of refilling bullet to its max capacity
    val bulletProps: BulletProps,
    open val colPoly: ColPoly,
    val offsetFromParentCenter: DVec2,
    val firingPosOffset : DVec2
) {
    open fun toWeapon(belongTo: GameElement) : Weapon {
        return Weapon(damage, minInterv, maxCapacity, reloadRate, bulletProps, colPoly, belongTo, offsetFromParentCenter, firingPosOffset)
    }
}

class RectWeaponProps(
    damage: Int,
    minInterv : Int,     // the minimum interval between two fires
    maxCapacity : Int,   // the maximum capacity of bullet in the weapon
    reloadRate : Double, // the rate of refilling bullet to its max capacity
    bulletProps: BulletProps,
    override val colPoly: ColRect,
    offsetFromParentCenter: DVec2 = 1.0.toYvec(),
    firingPosOffset : DVec2 = 1.1.toYvec()
) : WeaponProps(damage, minInterv, maxCapacity, reloadRate, bulletProps, colPoly, offsetFromParentCenter, firingPosOffset)
{
    override fun toWeapon(belongTo: GameElement) : RectWeapon {
        colPoly.rotationCenter = belongTo.colPoly.rotationCenter + belongTo.colPoly.encAARectSize.yVec / 2.0 * offsetFromParentCenter.y + belongTo.colPoly.encAARectSize.xVec  / 2.0 * offsetFromParentCenter.x
        return RectWeapon(damage, minInterv, maxCapacity, reloadRate, bulletProps, colPoly, belongTo, offsetFromParentCenter, firingPosOffset)
    }
    fun copy() : RectWeaponProps {
        return RectWeaponProps(damage, minInterv, maxCapacity, reloadRate, bulletProps.copy(), colPoly.copy(), offsetFromParentCenter.copy(), firingPosOffset.copy())
    }
}