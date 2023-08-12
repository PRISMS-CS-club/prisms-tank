package org.prismsus.tank.event

import kotlinx.serialization.json.*
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.elements.GameMap
import org.prismsus.tank.elements.Tank
import org.prismsus.tank.markets.UpgradeRecord
import org.prismsus.tank.utils.CompNum
import org.prismsus.tank.utils.collidable.ColMultiPart
import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.game
import org.prismsus.tank.utils.toEvtFixed
import org.prismsus.tank.utils.toFixed
import java.lang.System.currentTimeMillis

/**
 * Base class for all events.
 * @property timeStamp Timestamp of the event. The timestamp is the number of milliseconds since the start
 *                     of the game.
 */
abstract class GameEvent(val timeStamp: Long = game!!.elapsedGameMs) : Comparable<GameEvent> {
    abstract val serializedBytes : ByteArray
    open val serializedStr : String
        get() = serializedBytes.toString(Charsets.UTF_8)
    abstract val serialName : String
    override fun compareTo(other: GameEvent): Int {
        return timeStamp.compareTo(other.timeStamp)
    }
}


class MapCreateEvent (val map : GameMap, timeStamp : Long = game!!.elapsedGameMs) : GameEvent(timeStamp){
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

class ElementCreateEvent(val ele : GameElement, timeStamp : Long = game!!.elapsedGameMs) : GameEvent(timeStamp){
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
                put("x", selectBaseColPoly(ele).rotationCenter.x.toEvtFixed())
                put("y", selectBaseColPoly(ele).rotationCenter.y.toEvtFixed())
                put("rad", ele.colPoly.angleRotated.toEvtFixed())
                put("width", selectBaseColPoly(ele).width.toEvtFixed())
                put("height", selectBaseColPoly(ele).height.toEvtFixed())
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

class ElementUpdateEvent(val ele : GameElement, val updateEventMask: UpdateEventMask, timeStamp: Long = game!!.elapsedGameMs) : GameEvent(timeStamp){


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
                    put("x", selectBaseColPoly(ele).rotationCenter.x.toEvtFixed())
                }
                if (updateEventMask.y) {
                    put("y", selectBaseColPoly(ele).rotationCenter.y.toEvtFixed())
                }
                if (updateEventMask.rad) {
                    put("rad", selectBaseColPoly(ele).angleRotated.toEvtFixed())
                }
            }

            serializedBytes = json.toString().toByteArray()
        }
}

class ElementRemoveEvent(val uid : Long, timeStamp: Long = game!!.elapsedGameMs) : GameEvent(timeStamp){
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

class PlayerUpdateEvent(val uid : Long, timeStamp: Long = game!!.elapsedGameMs, vararg recs : UpgradeRecord<out Number>) : GameEvent(timeStamp){
    override val serialName: String = "PlrUpd"
    override val serializedBytes: ByteArray
    init{
        val json = buildJsonObject {
            put("type", serialName)
            put("t", timeStamp)
            put("uid", uid)
            for (rec in recs){
                put(rec.type.serialName, rec.value)
            }
        }
        serializedBytes = json.toString().toByteArray()
    }
}
