package org.prismsus.tank.event

import org.prismsus.tank.utils.binDeserializationFromJson
import org.prismsus.tank.utils.binSerializationToSendThroughJson
import org.prismsus.tank.utils.game
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class ServerResponseEvent(
    val returnValue: Any?,
    val requestSentTime: Long,
    val requestId: Long,
    timeStamp: Long,
) : DeserializableEvent(timeStamp) {
    override val serialName: String = "sRes"
    // TODO: implement deserialization

    init {
        jsonFieldNameToClassFieldName.putAll(
            mapOf(
                "rid" to "requestId",
                "sentT" to "requestSentTime",
                "retVal" to "returnValue"
            )
        )
        jsonValueToClassFieldValueFuncs.putAll(
            mapOf(
                "returnValue" to { it.binDeserializationFromJson() }
            )
        )

        classFieldValueToJsonValueFuncs.putAll(
            mapOf(
                "returnValue" to { it.binSerializationToSendThroughJson() }
            )
        )
    }
    companion object{
        operator fun invoke(serializedStr : String) : GameEvent = deserialize(serializedStr, ServerResponseEvent::class)
    }
}

class ServerSyncronizeEvent(
    val gameInitTime : Long,
    timeStamp : Long
) : DeserializableEvent(timeStamp){
    override val serialName: String = "sSync"
    init{
        jsonFieldNameToClassFieldName.putAll(
            mapOf(
                "InitT" to "gameInitTime"
            )
        )
    }
    companion object{
        operator fun invoke(serializedStr : String) : GameEvent = deserialize(serializedStr, ServerSyncronizeEvent::class)
    }
}