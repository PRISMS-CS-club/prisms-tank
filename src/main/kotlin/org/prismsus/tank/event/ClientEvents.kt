package org.prismsus.tank.event
import kotlinx.serialization.json.*
import java.util.*
import kotlin.collections.ArrayList

fun parseTimeStamp(str : String) : Long {
    val json = Json.parseToJsonElement(str)
    return json.jsonObject["t"]!!.jsonPrimitive.long
}

class GUIrequestEvent(override val serializedStr : String) : GameEvent(parseTimeStamp(serializedStr)){
    val funName : String
    val params : Array<*>
    val time : Long
    init{
        val scan = Scanner(serializedStr)
        time = scan.nextLong()
        funName = scan.next()
        val tmpParam = ArrayList<Any>()
        while(scan.hasNext()){
            if (scan.hasNextLong())
                tmpParam.add(scan.nextInt())
            else if (scan.hasNextDouble())
                tmpParam.add(scan.nextDouble())
            else
                tmpParam.add(scan.next())
        }
        params = tmpParam.toTypedArray()
    }
    override val serialName: String = "GUIreq"
    override val serializedBytes: ByteArray = serializedStr.toByteArray()
}