package org.prismsus.tank.elements

import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign


class Tank(
    uid: Long,
    val weaponProps: WeaponProps,
    val trackMaxSpeed: Double = INIT_TANK_TRACK_SPEED,
    hp: Int = INIT_TANK_HP,
    val tankRectBox: ColRect = INIT_TANK_COLBOX.copy(),
) :

    MovableElement(
        uid, hp, ColMultiPart((tankRectBox), (weaponProps.colPoly + DVec2(.0, tankRectBox.height / 2))!!)
    ) {

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
        val ddt = dt / 1000.0 // convert to second
        if (leftTrackVelo errEQ 0.0 && rightTrackVelo errEQ 0.0) return
        if (leftTrackVelo errEQ rightTrackVelo) {
            // the tank is moving straight
            val shiftVal = DVec2.byPolar(1.0, colPoly.angleRotated) * leftTrackVelo * ddt
            colPoly += shiftVal
//            weapon.colPoly += shiftVal
//            tankRectBox += shiftVal
            return
        }
        if (abs(leftTrackVelo) errEQ abs(rightTrackVelo)) {
            // the tank is rotating in place, left and right track speed must have different sign
            // meaing that the rotation center is the rotation center of the tank
            val angSign = if (rightTrackVelo > 0) 1 else -1
            val angVelo = abs(leftTrackVelo / .5)
            val angDisp = angVelo * ddt
            colPoly.rotateAssign(angDisp * angSign, tankRectBox.rotationCenter)
//            tankRectBox.rotateAssignTo(angDisp * angSign, tankRectBox.rotationCenter)
//            weapon.colPoly.rotateAssignTo(colPoly.angleRotated, tankRectBox.rotationCenter)
            return
        }

        val pivotBaseLine = if (isInnerCircLeft()) Line(tankRectBox.rightMidPt, tankRectBox.leftMidPt)
        else Line(tankRectBox.leftMidPt, tankRectBox.rightMidPt)
        val pivotPt = pivotBaseLine.atT(turningRad + 1.0) // make sure that we're shifting start from the ending point of that pivotBaseLine
        val angSign = if (leftTrackVelo - rightTrackVelo > 0) -1 else 1
        val angVelo = abs(inVelo / turningRad)
        val angDisp = angVelo * ddt
        colPoly.rotateAssign(angDisp * angSign, pivotPt)
//        tankRectBox.rotateAssignTo(colPoly.angleRotated, pivotPt)
//        weapon.colPoly.rotateAssignTo(colPoly.angleRotated, pivotPt)
    }

    override val serialName: String
        get() = "Tk"

    override fun willMove(dt: Long) : Boolean {
        if (dt == 0.toLong()) return false
        return leftTrackVelo errNE 0.0 || rightTrackVelo errNE 0.0
    }

    override fun colPolyAfterMove(dt: Long): ColPoly {
        val before = colPoly.copy() as ColMultiPart
        updateByTime(dt)
        val after = colPoly.copy()
        colPoly.becomeNonCopy(before)
        if (willMove(dt))
            assert(after != before)
        else assert(after == before)
        return after as ColPoly
    }

    companion object {
        fun byInitPos(
            uid: Long,
            initPos: DPos2,
            weaponProps: WeaponProps = INIT_RECT_WEAPON_RPOPS.copy(),
            trackMaxSpeed: Double = INIT_TANK_TRACK_SPEED,
            hp: Int = INIT_TANK_HP,
            rectBox: ColRect = INIT_TANK_COLBOX.copy()
        ): Tank {
            rectBox.rotationCenter = initPos.copy()
            weaponProps.colPoly.rotationCenter = initPos.copy()
            return Tank(uid, weaponProps, trackMaxSpeed, hp, rectBox)
        }
    }
}
