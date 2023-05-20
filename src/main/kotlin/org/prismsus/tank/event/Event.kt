package org.prismsus.tank.event
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * Base class for all events.
 * @property timestamp Timestamp of the event. The timestamp is the number of milliseconds since the start
 *                     of the game.
 */
abstract class Event(val timestamp: Long) {
    abstract fun serialize(): ByteArray
    abstract val serialName : String
}