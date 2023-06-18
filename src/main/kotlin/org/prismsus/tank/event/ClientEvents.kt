package org.prismsus.tank.event
import kotlinx.serialization.json.*
fun parseTimeStamp(str : String) : Long {
    val json = Json.parseToJsonElement(str)
    return json.jsonObject["t"]!!.jsonPrimitive.long
}

class UserInputEvent(override val serializedStr : String) : GameEvent(parseTimeStamp(serializedStr)){
    val ltrackSpeed : Double?
    val rtrackSpeed : Double?
    val shoot : Boolean?
    init{
        val json = Json.parseToJsonElement(serializedStr)
        ltrackSpeed = json.jsonObject["ltrackSpeed"]?.run{jsonPrimitive.double}
        rtrackSpeed = json.jsonObject["rtrackSpeed"]?.run{jsonPrimitive.double}
        shoot = json.jsonObject["shoot"]?.run{jsonPrimitive.boolean}
    }
    override val serialName: String = "UsrIpt"
    override val serializedBytes: ByteArray = serializedStr.toByteArray()
}