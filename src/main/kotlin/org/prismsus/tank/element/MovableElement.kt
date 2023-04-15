package org.prismsus.tank.element

import org.prismsus.tank.utils.ColBox
import org.prismsus.tank.utils.DVec2

abstract class MovableElement(uid: Long, hp: Int = -1, colBox: ColBox, var speed: Double = .0, var angVelo: Double = .0) :
    GameElement(uid, hp, colBox) {
    enum class TurnDir {
        LEFT, RIGHT
    }

    var curVelo : DVec2 = DVec2(.0, .0)
    var curAngV : Double = .0

    fun startMove(dir : DVec2, turnDir : TurnDir? = null){
        curVelo = dir.norm() * speed
        if (turnDir != null){
            curAngV = angVelo * if (turnDir == TurnDir.LEFT) -1 else 1
        }
    }

    fun stopMove(){
        curVelo = DVec2(.0, .0)
        curAngV = .0
    }

    fun updateTime(dt : Long){
        colBox = (colBox + curVelo * dt.toDouble()) as ColBox
        // center is the intersection of two diagonals
        var center = (colBox.pts[0] + colBox.pts[2]) / 2.0
        colBox.rotateAssign(center, curAngV * dt.toDouble())
    }

    }