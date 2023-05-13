package org.prismsus.tank.event
import org.prismsus.tank.element.block.*
class MapCreateEvent (timeStamp : Long) : Event(timeStamp){
    override fun serialize() : ByteArray {
        // TODO: implement, now just return empty array
        return ByteArray(0)
    }
    // TODO: implement, now set the timestamp to 0
    override val timeStamp : Long = 0
    override val serialName : String = "MapCrtEvt"
}