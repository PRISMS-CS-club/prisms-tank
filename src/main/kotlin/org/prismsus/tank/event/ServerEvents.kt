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
    val mp : MutableMap<String, Any> by lazy{
            val tmp = mutableMapOf<String, Any>()
            tmp.put("type", serialName)
            tmp.put("t", timeStamp)
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
    override val serialName: String = "MapCxrt"

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

class PlayerUpdateEvent(val uid: Long, timeStamp: Long = game!!.elapsedGameMs, vararg recs: UpgradeRecord<out Number>) :
    GameEvent(timeStamp) {
    override val serialName: String = "PlrUpd"
    init {
        val tmp = buildMap {
            put("uid", uid)
            for (rec in recs) {
                put(rec.type.serialName, rec.value.toEvtFixed())
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