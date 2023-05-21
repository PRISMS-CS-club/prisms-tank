package org.prismsus.tank.elements

import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.collidable.Line
import org.prismsus.tank.utils.collidable.ColRect
import kotlin.math.*


abstract class Tank(
    uid: Long,
    val trackMaxSpeed: Double = INIT_TANK_TRACK_SPEED,
    hp: Int = INIT_TANK_HP,
    colPoly: ColPoly = INIT_TANK_COLBOX,
    val weapon: Weapon
) :

    MovableElement(uid, hp, colPoly.union(weapon.colPoly + DVec2(.0, colPoly.height / 2))!!) {
    init {
        weapon.belongTo = this
    }

    val rectBox = colPoly as ColRect
    var leftTrackVelo: Double = .0
        set(value) {
            field = sign(leftTrackVelo) * min(abs(leftTrackVelo), trackMaxSpeed)
        }
    var rightTrackVelo: Double = .0
        set(value) {
            field = sign(rightTrackVelo) * min(abs(rightTrackVelo), trackMaxSpeed)
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
        if (leftTrackVelo errEQ rightTrackVelo) {
            // the tank is moving straight
            colPoly += DVec2.byPolar(1.0, colPoly.angleRotated) * leftTrackVelo * dt.toDouble()
            return
        }
        if (abs(leftTrackVelo) errEQ abs(rightTrackVelo)) {
            // the tank is rotating in place, left and right track speed must have different sign
            val angSign = if (rightTrackVelo > 0) 1 else -1
            val angVelo = abs(leftTrackVelo / .5)
            val angDisp = angVelo * dt.toDouble()
            colPoly.rotate(angDisp * angSign, rectBox.rotationCenter)
        }

        val pivotBaseLine = if (isInnerCircLeft()) Line(rectBox.rightMidPt, rectBox.leftMidPt)
        else Line(rectBox.leftMidPt, rectBox.rightMidPt)
        val pivotPt = pivotBaseLine.atT(turningRad)
        val angSign = if (leftTrackVelo - rightTrackVelo > 0) -1 else 1
        val angVelo = abs(inVelo / turningRad)
        val angDisp = angVelo * dt.toDouble()
        colPoly.rotate(angDisp * angSign, pivotPt)
        weapon.colPoly.angleRotated = colPoly.angleRotated
        weapon.colPoly.rotationCenter = colPoly.rotationCenter + weapon.centerOffset!!
    }

    override val serialName: String
        get() = "Tk"

}
