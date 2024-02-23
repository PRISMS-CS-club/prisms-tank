package org.prismsus.tank.event

import kotlinx.serialization.json.*
import org.prismsus.tank.utils.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@SyntheticNoArgConstruct
abstract class DeserializableEvent(timeStamp : Long) : GameEvent(timeStamp) {
    constructor() : this(-1)
    val jsonFieldNameToClassFieldName: MutableMap<String, String> = mutableMapOf(
        "type" to "serialName",
        "t" to "timeStamp"
    )
    val jsonValueToClassFieldValueFuncs: MutableMap<String, (JsonElement) -> Any> = mutableMapOf()
    // indexed by class field name
    val classFieldValueToJsonValueFuncs: MutableMap<String, (Any) -> JsonElement> = mutableMapOf()
    // indexed by class field name
    override val mpDelegate: LazyExtended<MutableMap<String, Any>>
        get() = lazyExtended {
            val tmp = super.mpDelegate.value
            tmp.putAll(buildMap{
                for ((jsonName, classFieldName) in jsonFieldNameToClassFieldName){
                    val classFunc = classFieldValueToJsonValueFuncs[classFieldName] ?: { it.toJsonElement() }
                    val value = this@DeserializableEvent.getMemberByName<Any>(classFieldName)
                    put(jsonName, classFunc(value))
                }
            })
            tmp
        }


    init{
        print("DeserializableEvent init")

    }

    open fun deserialize(serializedStr: String) : GameEvent
    {
        return deserializeGameEvent(
            this::class,
            serializedStr,
            jsonFieldNameToClassFieldName,
            jsonValueToClassFieldValueFuncs
        )
    }
}


fun <T : GameEvent> deserializeGameEvent(
    eventType: KClass<T>,
    serializedsStr: String,
    jsonFieldNameToClassFieldName: MutableMap<String, String> = mutableMapOf(),
    jsonValueToClassValueFuncs: MutableMap<String, (JsonElement) -> Any> = mutableMapOf(),
): T {
    val event = eventType.java.getConstructor().newInstance() as T
    val jsonMp = Json.parseToJsonElement(serializedsStr).jsonObject.toMutableMap()
    jsonFieldNameToClassFieldName["t"] = "timeStamp"
    for ((jsonName, jsonValue) in jsonMp.entries.iterator()) {
        if (jsonName == "type") continue // no need for serialName
        val memberName = jsonFieldNameToClassFieldName[jsonName] ?: jsonName
        val valueFunc = jsonValueToClassValueFuncs[memberName] ?: { it.toKotlinValues() }
        val memberValue = valueFunc(jsonValue)
        event.setPropertyByName(memberName, memberValue)
    }
    return event
}