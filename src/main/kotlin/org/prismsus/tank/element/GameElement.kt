package org.prismsus.tank.element;
import org.prismsus.tank.utils.collidable.ColBox
import kotlin.collections.ArrayList

abstract class GameElement(val uid : Long, var hp : Int = -1, open val colBox : ColBox) {
    var noCollisionElementIds = ArrayList<Long>()  // game elements that will not collide with this game element
                                                   // e.g. bullet shot by one player will not collide with itself
    enum class RemoveStat {
        REMOVED, TO_REMOVE, NOT_REMOVE
    }
    var removeStat = RemoveStat.NOT_REMOVE      // whether this game element is to be removed

    /**
     * called when detect two objects intersect with each other
     * @param other the other game element
     * @return whether the collision happened
     */
    fun processCollision(other : GameElement) : Boolean {
        if (other.uid in noCollisionElementIds) return false
        if (hp == -1) return false
        hp -= other.hp
        if (hp <= 0) {
            removeStat = RemoveStat.TO_REMOVE
        }
        return true
    }

    abstract val serialName : String
}