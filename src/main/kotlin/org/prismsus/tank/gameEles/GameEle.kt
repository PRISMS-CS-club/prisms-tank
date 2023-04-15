package org.prismsus.tank.gameEles;
import org.prismsus.tank.utils.ColBox
import java.util.*
import kotlin.collections.ArrayList

abstract class GameEle(val uid : Long, var hp : Int = -1, var colBox : ColBox) {
    var noColEleIds = ArrayList<Long>()  // game elements that will not collide with this game element
                                        // e.g. bullet shot by one player will not collide with itself
    enum class REM_STAT{
        REMED, TO_REM, NOT_REM
    }
    var remStat = REM_STAT.NOT_REM      // whether this game element is to be removed

    /**
     * called when detect two objects intersect with each other
     * @param other the other game element
     * @return whether the collision happened
     */
    fun processCollision(other : GameEle) : Boolean {
        if (other.uid in noColEleIds) return false
        if (hp == -1) return false
        hp -= other.hp
        if (hp <= 0) {
            remStat = REM_STAT.TO_REM
        }
        return true
    }
}