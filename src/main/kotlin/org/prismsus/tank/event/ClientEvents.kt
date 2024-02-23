package org.prismsus.tank.event
import org.prismsus.tank.utils.serializeByKyro
import java.util.*
import kotlin.collections.ArrayList
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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


class BotInitEvent(val name : String, val teamId : Long) : GameEvent(-1){
    override val serialName : String = "bInit"
    init{
        val tmp = buildMap {
            put("name", name)
            put("teamId", teamId)
        }
        mp.putAll(tmp)
    }
}

@OptIn(ExperimentalEncodingApi::class)
class BotRequestEvent(val reqType : String, rid : Long, timeStamp : Long, val params : Array<*>) : GameEvent(timeStamp){
    override val serialName : String = "bReq"
    init{
        val tmp = buildMap {
            put("type", reqType)
            put("rid", rid)
            if (params.isNotEmpty()) {
                val bytes = params.serializeByKyro()
                val base64 = Base64.encode(bytes)
                put("params", base64)
            }
        }
        mp.putAll(tmp)
    }
}