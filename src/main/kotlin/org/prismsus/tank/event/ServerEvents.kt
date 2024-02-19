package org.prismsus.tank.event

import kotlinx.serialization.json.*
import kotlinx.serialization.*
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.elements.GameMap
import org.prismsus.tank.elements.Tank
import org.prismsus.tank.markets.UpgradeEntry
import org.prismsus.tank.markets.UpgradeRecord
import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.ColMultiPart
import org.prismsus.tank.utils.collidable.ColPoly
import java.lang.System.currentTimeMillis
import java.util.Collections.addAll

/**
 * Base class for all events.
 * @property timeStamp Timestamp of the event. The timestamp is the number of milliseconds since the start
 *                     of the game.
 */
abstract class GameEvent(val timeStamp: Long = game!!.elapsedGameMs) : Comparable<GameEvent> {
    open val serializedBytes : ByteArray by lazy{serializedStr.toByteArray()}
    open val serializedStr: String by lazy{mp.toJsonString()}
    abstract val serialName: String
    val mp : MutableMap<String, Any> by lazy {
        val tmp = mutableMapOf<String, Any>()
        tmp["type"] = serialName
        tmp["t"] = timeStamp
        tmp
    }
    override fun compareTo(other: GameEvent): Int {
        return timeStamp.compareTo(other.timeStamp)
    }

}


class MapCreateEvent(val map: GameMap, timeStamp: Long = game!!.elapsedGameMs) : GameEvent(timeStamp) {
    override val serializedBytes: ByteArray
        get() = map.serialized
    override val serializedStr: String
        get() = map.serialized.decodeToString()
//    override val mp = throw NotImplementedError("this event does not support converting to map<str, any>")
    override val serialName: String = "MapCrt"
}

fun selectBaseColPoly(ele: GameElement): ColPoly {
    return if (ele.colPoly is ColMultiPart)
        (ele.colPoly as ColMultiPart).baseColPoly
    else
        ele.colPoly
}

class ElementCreateEvent(val ele: GameElement, timeStamp: Long = game!!.elapsedGameMs) : GameEvent(timeStamp) {
    override val serialName: String = "EleCrt"

    init {
        val tmp = buildMap {
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
        mp.putAll(tmp)
    }
}


data class UpdateEventMask(val hp: Boolean, val x: Boolean, val y: Boolean, val rad: Boolean) {

    fun any(): Boolean {
        return hp || x || y || rad
    }
    fun all(): Boolean {
        return hp && x && y && rad
    }
    fun trueCnt() : Int {
        return if (hp) 1 else 0 + if (x) 1 else 0 + if (y) 1 else 0 + if (rad) 1 else 0
    }

    fun falseCnt() : Int{
        return if (!hp) 1 else 0 + if (!x) 1 else 0 + if (!y) 1 else 0 + if (!rad) 1 else 0
    }
    companion object {
        fun defaultTrue(
            hp: Boolean = true,
            x: Boolean = true,
            y: Boolean = true,
            rad: Boolean = true
        ): UpdateEventMask {
            return UpdateEventMask(hp, x, y, rad)
        }

        fun defaultFalse(
            hp: Boolean = false,
            x: Boolean = false,
            y: Boolean = false,
            rad: Boolean = false
        ): UpdateEventMask {
            return UpdateEventMask(hp, x, y, rad)
        }
    }
}

class ElementUpdateEvent(
    val ele: GameElement,
    val updateEventMask: UpdateEventMask,
    timeStamp: Long = game!!.elapsedGameMs
) : GameEvent(timeStamp) {


    override val serialName: String = "EleUpd"

    init {
        val tmp = buildMap {
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
        if(!(updateEventMask.hp || updateEventMask.x || updateEventMask.y || updateEventMask.rad)) {
            println("DEBUG: $serializedStr")
        }
        mp.putAll(tmp)
    }
}

class ElementRemoveEvent(val uid: Long, timeStamp: Long = game!!.elapsedGameMs) : GameEvent(timeStamp) {
    override val serialName: String = "EleRmv"

    init {
        val tmp = buildMap {
            put("uid", uid)
        }
        mp.putAll(tmp)
    }
}

class PlayerUpdateEvent(val uid: Long, timeStamp: Long = game!!.elapsedGameMs, vararg recs: UpgradeRecord<out Number>, dbgStr : String? = null ) :
    GameEvent(timeStamp) {
    override val serialName: String = "PlrUpd"
    init {
        val tmp = buildMap {
            put("uid", uid)
            for (rec in recs) {
                put(rec.type.serialName, rec.value.toEvtFixed())
            }
            if (dbgStr != null){
                put("dbgStr", dbgStr)
            }
        }
        mp.putAll(tmp)
    }
}

class GameEndEvent(rankMap: Map<Long, Long>, timeStamp : Long = game!!.elapsedGameMs) : GameEvent(timeStamp){
    // rankMap is mapping from uid to ranking
    override val serialName : String = "End"
    init {
        val tmp = buildMap {
            put("uids", rankMap.keys.toTypedArray().toJsonElement())
            put("ranks", rankMap.values.toTypedArray().toJsonElement())
        }
        mp.putAll(tmp)
    }

}


class DebugEvent(val msg: String, val debugType: DebugType, timeStamp: Long = game!!.elapsedGameMs) : GameEvent(timeStamp) {
    override val serialName: String = "Dbg"
    enum class DebugType(val serialName: String, val severity : Int) {
        TRACE("TRACE", 0),
        DEBUG("DEBUG", 1),
        INFO("INFO", 2),
        WARN("WARN", 3),
        ERROR("ERROR", 4),
        FATAL("FATAL", 5)
    }
    init {
        val tmp = buildMap {
            put("msg", msg)
            put("level", DebugType.INFO.serialName)
            put("severity", debugType.severity)
        }
        mp.putAll(tmp)
        if (debugType.severity >= printIfAboveOrEqual.severity) {
            println("DEBUG: $serializedStr")
        }
    }
    companion object{
        val printIfAboveOrEqual = DebugType.INFO
        val storeIfAboveOrEqual = DebugType.WARN
    }
}

object INIT_EVENT : GameEvent(0) {
    override val serialName: String = "Init"

    init {
        val tmp = buildMap{
            put("plr",
                buildMap {
                    for (tp in UpgradeEntry.UpgradeType.values()) {
                        put(tp.serialName, tp.defaultValue)
                    }
                })
            put("pricingRule", defAuction.type.serialName)
            // cannot use game!!.marketImpl here, because when sending this event
            // game is not started yet.
        }
        mp.putAll(tmp)
    }
}