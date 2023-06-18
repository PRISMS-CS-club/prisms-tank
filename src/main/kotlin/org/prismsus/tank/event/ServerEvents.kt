package org.prismsus.tank.event
import kotlinx.serialization.json.*
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.elements.GameMap
import org.prismsus.tank.utils.collidable.ColMultiPart
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
    // TODO: implement, now set the timestamp to 0
    override val serialName : String = "MapCrt"
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
                if (ele.colPoly is ColMultiPart)
                    put("x", (ele.colPoly as ColMultiPart).baseColPoly.rotationCenter.x)
                else
                    put("x", ele.colPoly.rotationCenter.x)
                if (ele.colPoly is ColMultiPart)
                    put("y", (ele.colPoly as ColMultiPart).baseColPoly.rotationCenter.y)
                else
                    put("y", ele.colPoly.rotationCenter.y)
                put("rad", ele.colPoly.angleRotated)
                put("width", ele.colPoly.width)
                put("height", ele.colPoly.height)
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
                    if (ele.colPoly is ColMultiPart)
                        put("x", (ele.colPoly as ColMultiPart).baseColPoly.rotationCenter.x)
                    else put("x", ele.colPoly.rotationCenter.x)
                }
                if (updateEventMask.y) {
                    if (ele.colPoly is ColMultiPart)
                        put("y", (ele.colPoly as ColMultiPart).baseColPoly.rotationCenter.y)
                    else put("y", ele.colPoly.rotationCenter.y)
                }
                if (updateEventMask.rad) {
                    put("rad", ele.colPoly.angleRotated)
                }
            }

            serializedBytes = json.toString().toByteArray()
        }
}
