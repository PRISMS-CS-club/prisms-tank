package org.prismsus.tank.event
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


