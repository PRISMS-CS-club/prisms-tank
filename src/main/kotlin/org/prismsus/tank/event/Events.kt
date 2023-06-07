package org.prismsus.tank.event
import kotlinx.serialization.json.*
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.elements.GameMap
import org.prismsus.tank.elements.MultiPartElement
import java.lang.System.currentTimeMillis
/**
 * Base class for all events.
 * @property timeStamp Timestamp of the event. The timestamp is the number of milliseconds since the start
 *                     of the game.
 */
abstract class GameEvent(val timeStamp: Long = currentTimeMillis()) : Comparable<GameEvent> {
    abstract val serialized : ByteArray
    abstract val serialName : String
    override fun compareTo(other: GameEvent): Int {
        return timeStamp.compareTo(other.timeStamp)
    }
}


class MapCreateEvent (val map : GameMap, timeStamp : Long = currentTimeMillis()) : GameEvent(timeStamp){
    override val serialized: ByteArray
        get() = map.serialized
    // TODO: implement, now set the timestamp to 0
    override val serialName : String = "MapCrt"
}

class ElementCreateEvent(val ele : GameElement, timeStamp : Long = currentTimeMillis()) : GameEvent(timeStamp){
    override val serialized: ByteArray
        get() {
            val json = buildJsonObject {
                put("type", serialName)
                put("t", timeStamp)
                put("uid", ele.uid)
                put("name", ele.serialName)
                put("x", ele.colPoly.rotationCenter.x)
                put("y", ele.colPoly.rotationCenter.y)
                put("rad", ele.colPoly.angleRotated)
                put("width", ele.colPoly.width)
                put("height", ele.colPoly.height)
            }
            return json.toString().toByteArray()
        }
    override val serialName: String = "EleCrt"
}


data class UpdateEventSlect(val hp : Boolean, val x : Boolean, val y : Boolean, val rad : Boolean){
    companion object{
        fun defaultTrue(hp : Boolean = true, x : Boolean = true, y : Boolean = true, rad : Boolean = true) : UpdateEventSlect{
            return UpdateEventSlect(hp, x, y, rad)
        }
        fun defaultFalse(hp : Boolean = false, x : Boolean = false, y : Boolean = false, rad : Boolean = false) : UpdateEventSlect{
            return UpdateEventSlect(hp, x, y, rad)
        }
    }
}

class ElementUpdateEvent(val ele : GameElement, val updateEventSlect: UpdateEventSlect, timeStamp: Long = currentTimeMillis()) : GameEvent(timeStamp){
    override val serialized: ByteArray
        get() {
            val json = buildJsonObject {
                put("type", serialName)
                put("t", timeStamp)
                put("uid", ele.uid)
                if (updateEventSlect.hp)
                    put("hp", ele.hp)
                if (updateEventSlect.x)
                    if (ele is MultiPartElement)
                        put("x", ele.baseColPoly.rotationCenter.x)
                    else put("x", ele.colPoly.rotationCenter.x)
                if (updateEventSlect.y)
                    if (ele is MultiPartElement)
                        put("y", ele.baseColPoly.rotationCenter.y)
                    else put("y", ele.colPoly.rotationCenter.y)
                if (updateEventSlect.rad)
                    put("rad", ele.colPoly.angleRotated)
            }

            return json.toString().toByteArray()
        }
    override val serialName: String = "EleUpd"
}
