package org.prismsus.tank.elements

import org.prismsus.tank.utils.DOUBLE_PRECISION
import org.prismsus.tank.utils.DVec2
import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.collidable.DPos2
import org.prismsus.tank.utils.collidable.ColRect
import org.prismsus.tank.utils.nextUid

open class Weapon(
    val damage : Int,
    val minInterv : Int,     // the minimum interval between two fires, in ms
    val maxCapacity : Int,   // the maximum capacity of bullet in the weapon
    val reloadRate : Double, // the rate of refilling bullet to its max capacity, per second
    val bulletProps : BulletProps,
    override val colPoly: ColPoly,
    override var belongTo: GameElement,
    override var centerOffset: DVec2,
    val firingPos : DPos2,
) : SubGameElement, TimeUpdatable {
    override val serialName: String
        get() = "Wep"
    var curCapacity : Int = maxCapacity
    private var lastFireTime : Long = 0

    fun fire() : Bullet? {
        if (System.currentTimeMillis() - lastFireTime < minInterv) return null
        val bullet = Bullet(nextUid, bulletProps)
        bullet.colPoly.rotateTo(belongTo.colPoly.angleRotated)
        bullet.colPoly.bottomMidPt = firingPos
        bullet.curVelo = DVec2.byPolar(belongTo.colPoly.angleRotated, bulletProps.speed)
        bullet.damage = damage
        lastFireTime = System.currentTimeMillis()
        curCapacity--
        return bullet
    }

    override fun updateByTime(dt: Long) {
        curCapacity += (reloadRate * dt * 1000).toInt()
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
    centerOffset: DVec2 = DVec2(.0, belongTo.colPoly.height / 2.0),
    firingPos : DPos2 =  colBox.topMidPt + centerOffset + DVec2(0.0,  DOUBLE_PRECISION * 100)
) : Weapon(damage, minInterv, maxCapacity, reloadRate,  bulletProps, colBox, belongTo, centerOffset, firingPos)
{
}

open class WeaponProps(
    val damage: Int,
    val minInterv : Int,     // the minimum interval between two fires
    val maxCapacity : Int,   // the maximum capacity of bullet in the weapon
    val reloadRate : Double, // the rate of refilling bullet to its max capacity
    val bulletProps: BulletProps,
    val colPoly: ColPoly,
    val centerOffset: DVec2,
    val firingPos : DPos2
) {
    open fun toWeapon(belongTo: GameElement) : Weapon {
        return Weapon(damage, minInterv, maxCapacity, reloadRate, bulletProps, colPoly, belongTo, centerOffset, firingPos)
    }
}

class RectWeaponProps(
    damage: Int,
    minInterv : Int,     // the minimum interval between two fires
    maxCapacity : Int,   // the maximum capacity of bullet in the weapon
    reloadRate : Double, // the rate of refilling bullet to its max capacity
    bulletProps: BulletProps,
    val colBox: ColRect,
    centerOffset: DVec2 = DVec2(.0, colBox.height / 2.0),
    firingPos : DPos2 =  colBox.topMidPt + centerOffset + DVec2(0.0,  DOUBLE_PRECISION * 100)
) : WeaponProps(damage, minInterv, maxCapacity, reloadRate, bulletProps, colBox, centerOffset, firingPos)
{
    override fun toWeapon(belongTo: GameElement) : RectWeapon {
        return RectWeapon(damage, minInterv, maxCapacity, reloadRate, bulletProps, colBox, belongTo, centerOffset, firingPos)
    }
    fun copy() : RectWeaponProps {
        return RectWeaponProps(damage, minInterv, maxCapacity, reloadRate, bulletProps.copy(), colBox.copy(), centerOffset.copy(), firingPos.copy())
    }
}