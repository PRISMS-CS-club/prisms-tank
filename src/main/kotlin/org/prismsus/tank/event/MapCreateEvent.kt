package org.prismsus.tank.event
import org.prismsus.tank.element.Block
import org.prismsus.tank.element.GameMap

class MapCreateEvent (timeStamp : Long, map : GameMap) : Event(timeStamp){
    override fun serialize() : ByteArray {
        // TODO: implement, now just return empty array
        return ByteArray(0)
    }
    // TODO: implement, now set the timestamp to 0
    override val serialName : String = "MapCrtEvt"
}