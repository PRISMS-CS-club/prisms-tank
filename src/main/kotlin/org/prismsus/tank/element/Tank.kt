package org.prismsus.tank.element

import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.intersectables.Line
import org.prismsus.tank.utils.intersectables.RectColBox
import kotlin.math.*

class Tank(uid : Long, val trackMaxSpeed : Double, hp : Int, colBox : RectColBox) :
    MovableElement(uid, hp, colBox){
        val rectBox = colBox as RectColBox
        var leftTrackVelo : Double = .0
            set(value){
                field = sign(leftTrackVelo) * min(abs(leftTrackVelo), trackMaxSpeed)
            }
        var rightTrackVelo : Double = .0
            set(value){
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
        var turningRad : Double
            get(){
                val trackDis = 1.0
                return trackDis * inVelo / (outVelo - inVelo)
            }
            set(value) {}
    fun isInnerCircLeft() : Boolean {
        return abs(leftTrackVelo) < abs(rightTrackVelo)
    }

    var inVelo : Double
        get() = if (isInnerCircLeft()) leftTrackVelo else rightTrackVelo
        set(value){}
    var outVelo : Double
        get() = if (isInnerCircLeft()) rightTrackVelo else leftTrackVelo
        set(value){}
    override fun updatePosByTime(dt: Long) {
        if (abs(leftTrackVelo - rightTrackVelo) < DOUBLE_PRECISION){
            // the tank is moving straight
            colBox += DVec2.byPolarCoord(1.0, colBox.angleRotated) * leftTrackVelo * dt.toDouble()
            return
        }
        if (abs(leftTrackVelo) - abs(rightTrackVelo) < DOUBLE_PRECISION){
            // the tank is rotating in place, left and right track speed must have different sign
            val angSign = if (rightTrackVelo > 0) 1 else -1
            val angVelo = abs(leftTrackVelo / .5)
            val angDisp = angVelo * dt.toDouble()
            colBox.rotate(angDisp * angSign, rectBox.rotationCenter)
        }

        val pivotBaseLine = if (isInnerCircLeft()) Line(rectBox.rightMidPt, rectBox.leftMidPt)
                            else Line(rectBox.leftMidPt, rectBox.rightMidPt)
        val pivotPt = pivotBaseLine.atT(turningRad)
        val angSign = if (leftTrackVelo - rightTrackVelo > 0) -1 else 1
        val angVelo = abs(inVelo / turningRad)
        val angDisp = angVelo * dt.toDouble()
        colBox.rotate(angDisp * angSign, pivotPt)
    }

    }