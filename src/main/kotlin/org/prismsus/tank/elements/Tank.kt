package org.prismsus.tank.elements

import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.Line
import org.prismsus.tank.utils.collidable.ColRect
import org.prismsus.tank.utils.collidable.DPos2
import kotlin.math.*


class Tank(
    uid: Long,
    val weaponProps: WeaponProps,
    val trackMaxSpeed: Double = INIT_TANK_TRACK_SPEED,
    hp: Int = INIT_TANK_HP,
    val tankRectBox: ColRect = INIT_TANK_COLBOX,
) :

    MovableElement(uid, hp, (tankRectBox).union(weaponProps.colPoly + DVec2(.0, tankRectBox.height / 2))!!) {

    var weapon: Weapon

    init {
        weapon = weaponProps.toWeapon(this)
    }

    var leftTrackVelo: Double = .0
        set(value) {
            field = sign(value) * min(abs(value), trackMaxSpeed)
        }
    var rightTrackVelo: Double = .0
        set(value) {
            field = sign(value) * min(abs(value), trackMaxSpeed)
        }


    /*
    * The turning of tank can be modeled as two concentric circles with different radius
    * The inner circle is the track with smaller speed
    * The outer circle is the track with larger speed, hence the tank will turn to the direction of the track with smaller speed
    * This function returns the radius of the inner circle
    * If the return value is negative, means the center of rotation is in the body of tank
    * Assume the distance between two tracks is 1
    * */
    var turningRad: Double
        get() {
            val trackDis = 1.0
            return trackDis * inVelo / (outVelo - inVelo)
        }
        set(value) {}

    fun isInnerCircLeft(): Boolean {
        return abs(leftTrackVelo) < abs(rightTrackVelo)
    }

    var inVelo: Double
        get() = if (isInnerCircLeft()) leftTrackVelo else rightTrackVelo
        set(value) {}
    var outVelo: Double
        get() = if (isInnerCircLeft()) rightTrackVelo else leftTrackVelo
        set(value) {}

    override fun updateByTime(dt: Long) {
        if (leftTrackVelo errEQ 0.0 && rightTrackVelo errEQ 0.0) return
        if (leftTrackVelo errEQ rightTrackVelo) {
            // the tank is moving straight
            val shiftVal = DVec2.byPolar(1.0, colPoly.angleRotated) * leftTrackVelo * dt.toDouble()
            colPoly += shiftVal
            weapon.colPoly += shiftVal
            tankRectBox += shiftVal
            return
        }
        if (abs(leftTrackVelo) errEQ abs(rightTrackVelo)) {
            // the tank is rotating in place, left and right track speed must have different sign
            // meaing that the rotation center is the rotation center of the tank
            val angSign = if (rightTrackVelo > 0) 1 else -1
            val angVelo = abs(leftTrackVelo / .5)
            val angDisp = angVelo * dt.toDouble()
            colPoly.rotate(angDisp * angSign, tankRectBox.rotationCenter)
            tankRectBox.rotate(angDisp * angSign, tankRectBox.rotationCenter)
            weapon.colPoly.rotateTo(colPoly.angleRotated, tankRectBox.rotationCenter)
            return
        }

        val pivotBaseLine = if (isInnerCircLeft()) Line(tankRectBox.rightMidPt, tankRectBox.leftMidPt)
        else Line(tankRectBox.leftMidPt, tankRectBox.rightMidPt)
        val pivotPt = pivotBaseLine.atT(turningRad)
        val angSign = if (leftTrackVelo - rightTrackVelo > 0) -1 else 1
        val angVelo = abs(inVelo / turningRad)
        val angDisp = angVelo * dt.toDouble()
        colPoly.rotate(angDisp * angSign, pivotPt)
        tankRectBox.rotateTo(colPoly.angleRotated, pivotPt)
        weapon.colPoly.rotateTo(colPoly.angleRotated, pivotPt)
    }

    override val serialName: String
        get() = "Tk"

    companion object {
        fun byInitPos(
            uid: Long,
            initPos: DPos2,
            weaponProps: WeaponProps = INIT_RECT_WEAPON_RPOPS,
            trackMaxSpeed: Double = INIT_TANK_TRACK_SPEED,
            hp: Int = INIT_TANK_HP,
            rectBox: ColRect = INIT_TANK_COLBOX
        ): Tank {
            rectBox.rotationCenter = initPos
            weaponProps.colPoly.rotationCenter = initPos
            return Tank(uid, weaponProps, trackMaxSpeed, hp, rectBox)
        }
    }
}
