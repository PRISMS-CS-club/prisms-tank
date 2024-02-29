package org.prismsus.tank.event

import org.prismsus.tank.utils.binDeserializationFromJson
import org.prismsus.tank.utils.binSerializationToSendThroughJson
import org.prismsus.tank.utils.companionClass
import kotlin.reflect.KClass

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
    companion object{
        operator fun invoke(serializedStr : String) : GameEvent = deserialize(serializedStr, DeserializableEvent::class)
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
    companion object{
        operator fun invoke(serializedStr : String) : GameEvent = deserialize(serializedStr, BotRequestEvent::class)
    }
}