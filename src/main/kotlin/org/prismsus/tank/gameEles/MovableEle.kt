package org.prismsus.tank.gameEles

import org.prismsus.tank.utils.ColBox
import org.prismsus.tank.utils.Dvec2
import org.prismsus.tank.utils.Line

abstract class MovableEle(uid: Long, hp: Int = -1, colBox: ColBox, var speed: Double = .0, var angVelo: Double = .0) :
    GameEle(uid, hp, colBox) {
    enum class TURN_DIR {
        LF, RT
    }

    var curVelo : Dvec2 = Dvec2(.0, .0)
    var curAngV : Double = .0

    fun startMove(dir : Dvec2, turnDir : TURN_DIR? = null){
        curVelo = dir.norm() * speed
        if (turnDir != null){
            curAngV = angVelo * if (turnDir == TURN_DIR.LF) -1 else 1
        }
    }

    fun stopMove(){
        curVelo = Dvec2(.0, .0)
        curAngV = .0
    }

    fun updPos(dt : Long){
        colBox = (colBox + curVelo * dt.toDouble()) as ColBox
        // center is the intersection of two diagonals
        var center = (colBox.pts[0] + colBox.pts[2]) / 2.0
        colBox.rotateAssign(center, curAngV * dt.toDouble())
    }

    }