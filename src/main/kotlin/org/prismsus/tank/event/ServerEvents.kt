package org.prismsus.tank.event

import kotlinx.serialization.json.*
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.elements.GameMap
import org.prismsus.tank.elements.Tank
import org.prismsus.tank.utils.FixedPoint
import org.prismsus.tank.utils.collidable.ColMultiPart
import org.prismsus.tank.utils.collidable.ColPoly
import java.lang.System.currentTimeMillis

/**
 * Base class for all events.
 * @property timeStamp Timestamp of the event. The timestamp is the number of milliseconds since the start
 *                     of the game.
 */
abstract class GameEvent(val timeStamp: Long = currentTimeMillis()) : Comparable<GameEvent> {
    abstract val serializedBytes : ByteArray
    open val serializedStr : String
        get() = serializedBytes.toString(Charsets.UTF_8)
    abstract val serialName : String
    override fun compareTo(other: GameEvent): Int {
        return timeStamp.compareTo(other.timeStamp)
    }
}


class MapCreateEvent (val map : GameMap, timeStamp : Long = currentTimeMillis()) : GameEvent(timeStamp){
    override val serializedBytes: ByteArray
        get() = map.serialized
    override val serialName : String = "MapCrt"
}

fun selectBaseColPoly(ele : GameElement) : ColPoly{
    return if (ele.colPoly is ColMultiPart)
        (ele.colPoly as ColMultiPart).baseColPoly
    else
        ele.colPoly
}

class ElementCreateEvent(val ele : GameElement, timeStamp : Long = currentTimeMillis()) : GameEvent(timeStamp){
    override val serialName: String = "EleCrt"
    override val serializedBytes: ByteArray
        init{
            val json = buildJsonObject {
                put("type", serialName)
                put("t", timeStamp)
                put("uid", ele.uid)
                put("name", ele.serialName)
                if (ele is Tank)
                    put("player", ele.playerName)
                put("x", FixedPoint(selectBaseColPoly(ele).rotationCenter.x, 3))
                put("y", FixedPoint(selectBaseColPoly(ele).rotationCenter.y, 3))
                put("rad", FixedPoint(ele.colPoly.angleRotated, 3))
                put("width", FixedPoint(selectBaseColPoly(ele).width, 3))
                put("height", FixedPoint(selectBaseColPoly(ele).height, 3))
            }
           serializedBytes = json.toString().toByteArray()
        }
}


data class UpdateEventMask(val hp : Boolean, val x : Boolean, val y : Boolean, val rad : Boolean){
    companion object{
        fun defaultTrue(hp : Boolean = true, x : Boolean = true, y : Boolean = true, rad : Boolean = true) : UpdateEventMask{
            return UpdateEventMask(hp, x, y, rad)
        }
        fun defaultFalse(hp : Boolean = false, x : Boolean = false, y : Boolean = false, rad : Boolean = false) : UpdateEventMask{
            return UpdateEventMask(hp, x, y, rad)
        }
    }
}

class ElementUpdateEvent(val ele : GameElement, val updateEventMask: UpdateEventMask, timeStamp: Long = currentTimeMillis()) : GameEvent(timeStamp){


    override val serialName: String = "EleUpd"
    override val serializedBytes: ByteArray
        init{
            val json = buildJsonObject {
                put("type", serialName)
                put("t", timeStamp)
                put("uid", ele.uid)
                if (updateEventMask.hp) {
                    put("hp", ele.hp)
                }
                if (updateEventMask.x) {
                    put("x", FixedPoint(selectBaseColPoly(ele).rotationCenter.x, 3))
                }
                if (updateEventMask.y) {
                    put("y", FixedPoint(selectBaseColPoly(ele).rotationCenter.y, 3))
                }
                if (updateEventMask.rad) {
                    put("rad", FixedPoint(ele.colPoly.angleRotated, 3))
                }
            }

            serializedBytes = json.toString().toByteArray()
        }
}

class ElementRemoveEvent(val uid : Long, timeStamp: Long = currentTimeMillis()) : GameEvent(timeStamp){
    override val serialName: String = "EleRmv"
    override val serializedBytes: ByteArray
        init{
            val json = buildJsonObject {
                put("type", serialName)
                put("t", timeStamp)
                put("uid", uid)
            }
            serializedBytes = json.toString().toByteArray()
        }
}