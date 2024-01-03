package org.prismsus.tank.elements;
import org.prismsus.tank.utils.collidable.ColPoly
import kotlin.collections.ArrayList

abstract class GameElement(val uid : Long, var hp : Int = -1, open val colPoly : ColPoly) {
    constructor() : this(-1, -1, ColPoly()) // used for serialization
    var noCollisionElementIds = ArrayList<Long>()  // game elements that will not collide with this game element
                                                   // e.g. bullet shot by one player will not collide with itself
    enum class RemoveStat {
        REMOVED, TO_REMOVE, NOT_REMOVE
    }
    var removeStat = RemoveStat.NOT_REMOVE      // whether this game element is to be removed

    /**
     * called when detect two objects intersect with each other
     * @param other the other game element
     * @return whether the state of the [GameElement] is changed
     */
    open infix fun processCollision(other : GameElement) : Boolean {
        if (other.uid in noCollisionElementIds) return false
        if (hp == -1 || other !is Bullet) return false
        hp -= other.damage
        if (hp <= 0) {
            removeStat = RemoveStat.TO_REMOVE
        }
        return other.damage > 0
    }

    override fun hashCode(): Int {
        return uid.toInt()
    }

    abstract val serialName : String
}