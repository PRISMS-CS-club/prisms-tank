package org.prismsus.tank.event
import org.prismsus.tank.utils.binDeserializationFromJson
import org.prismsus.tank.utils.binSerializationToSendThroughJson
import java.util.*
import kotlin.collections.ArrayList

fun parseTimeStamp(str : String) : Long {
    val scan = Scanner(str)
    return scan.nextFloat().toLong()
}

class GUIrequestEvent(override val serializedStr : String) : GameEvent(parseTimeStamp(serializedStr)){
    val funName : String
    val params : Array<*>
    init{
        val scan = Scanner(serializedStr)
        scan.next() // skip timestamp
        funName = scan.next()
        val tmpParam = ArrayList<Any>()
        while(scan.hasNext()){
            if (scan.hasNextLong())
                tmpParam.add(scan.nextLong())
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


class BotInitEvent(val name : String, val teamId : Long) : DeserializableEvent(){
    override val serialName : String = "bInit"
    init{
        jsonFieldNameToClassFieldName.putAll(
            mapOf(
                "name" to "name",
                "teamId" to "teamId"
            )
        )

    }
}

class BotRequestEvent(val requestType : String, val requestId : Long, timeStamp : Long, val params : Array<*>) : DeserializableEvent(timeStamp){
    override val serialName : String = "bReq"
    init{

        jsonFieldNameToClassFieldName.putAll(
            mapOf(
                "reqType" to "requestType",
                "rid" to "requestId",
                "params" to "params"
            )
        )

        jsonValueToClassFieldValueFuncs.putAll(
            mapOf(
                "params" to {it.binDeserializationFromJson()}
            )
        )

        classFieldValueToJsonValueFuncs.putAll(
            mapOf(
                "params" to {it.binSerializationToSendThroughJson()}
            )
        )


    }
}