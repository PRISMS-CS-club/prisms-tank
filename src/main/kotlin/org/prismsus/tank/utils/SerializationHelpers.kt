package org.prismsus.tank.utils

import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import kotlin.io.encoding.Base64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.reflect.full.createType

fun Int.toBytesBigEnd() : ByteArray {
    return byteArrayOf((this shr 24).toByte(), (this shr 16).toByte(), (this shr 8).toByte(), this.toByte())
}

fun Int.toBytesLittleEnd() : ByteArray {
    return this.toBytesBigEnd().reversedArray()
}

fun Long.toBytesBigEnd() : ByteArray {
    return byteArrayOf(
        (this shr 56).toByte(), (this shr 48).toByte(), (this shr 40).toByte(), (this shr 32).toByte(),
        (this shr 24).toByte(), (this shr 16).toByte(), (this shr 8).toByte(), this.toByte()
    )
}

fun Long.toBytesLittleEnd() : ByteArray {
    return this.toBytesBigEnd().reversedArray()
}

fun Float.toBytesBigEnd() : ByteArray {
    return this.toRawBits().toBytesBigEnd()
}

fun Float.toBytesLittleEnd() : ByteArray {
    return this.toRawBits().toBytesLittleEnd()
}

fun Double.toBytesBigEnd() : ByteArray {
    return this.toRawBits().toBytesBigEnd()
}

fun Double.toBytesLittleEnd() : ByteArray {
    return this.toRawBits().toBytesLittleEnd()
}

fun ByteArray.toIntBigEnd() : Int {
    assert(this.size == 4)
    return (this[0].toInt() shl 24) or (this[1].toInt() shl 16) or (this[2].toInt() shl 8) or this[3].toInt()
}

fun ByteArray.toIntLittleEnd() : Int {
    return this.reversedArray().toIntBigEnd()
}

fun ByteArray.toLongBigEnd() : Long {
    assert(this.size == 8)
    return (this[0].toLong() shl 56) or (this[1].toLong() shl 48) or (this[2].toLong() shl 40) or (this[3].toLong() shl 32) or
            (this[4].toLong() shl 24) or (this[5].toLong() shl 16) or (this[6].toLong() shl 8) or this[7].toLong()
}

fun ByteArray.toLongLittleEnd() : Long {
    return this.reversedArray().toLongBigEnd()
}

fun ByteArray.toFloatBigEnd() : Float {
    assert (this.size == 4)
    val buffer = ByteBuffer.wrap(this).order(java.nio.ByteOrder.BIG_ENDIAN)
    return buffer.getFloat()
}

fun ByteArray.toFloatLittleEnd() : Float {
    return this.reversedArray().toFloatBigEnd()
}

fun ByteArray.toDoubleBigEnd() : Double {
    assert (this.size == 8)
    val buffer = ByteBuffer.wrap(this).order(java.nio.ByteOrder.BIG_ENDIAN)
    return buffer.getDouble()
}

fun ByteArray.toDoubleLittleEnd() : Double {
    return this.reversedArray().toDoubleBigEnd()
}

fun InputStream.readIntBigEnd() : Int {
    val buffer = ByteArray(4)
    this.read(buffer)
    return buffer.toIntBigEnd()
}

fun InputStream.readIntLittleEnd() : Int {
    val buffer = ByteArray(4)
    this.read(buffer)
    return buffer.toIntLittleEnd()
}

fun InputStream.readLongBigEnd() : Long {
    val buffer = ByteArray(8)
    this.read(buffer)
    return buffer.toLongBigEnd()
}

fun InputStream.readLongLittleEnd() : Long {
    val buffer = ByteArray(8)
    this.read(buffer)
    return buffer.toLongLittleEnd()
}

fun InputStream.readFloatBigEnd() : Float {
    val buffer = ByteArray(4)
    this.read(buffer)
    return buffer.toFloatBigEnd()
}

fun InputStream.readFloatLittleEnd() : Float {
    val buffer = ByteArray(4)
    this.read(buffer)
    return buffer.toFloatLittleEnd()
}

fun InputStream.readDoubleBigEnd() : Double {
    val buffer = ByteArray(8)
    this.read(buffer)
    return buffer.toDoubleBigEnd()
}

fun InputStream.readDoubleLittleEnd() : Double {
    val buffer = ByteArray(8)
    this.read(buffer)
    return buffer.toDoubleLittleEnd()
}

fun<T> T.deepCopyByKyro() : T{
    return thSafeKyro.copy(this)
}

fun<T> T.serializeByKyro() : ByteArray{
    val out = Output(2048, -1)
    thSafeKyro.writeClassAndObject(out, this)
    out.close()
    return out.toBytes()
}

fun ByteArray.deserializeByKyro() : Any{
    val input = Input(this)
    val ret = thSafeKyro.readClassAndObject(input)
    input.close()
    return ret
}

fun ByteArray.deflate(compressionLevel : Int = Deflater.BEST_SPEED) : ByteArray{
    val deflater = Deflater(compressionLevel)
    deflater.setInput(this)
    deflater.finish()
    val buffer = ByteArray(this.size)
    val size = deflater.deflate(buffer)

    return buffer.copyOf(size)
}

fun ByteArray.inflate() : ByteArray{
    val inflater = Inflater()
    inflater.setInput(this)
    val buffer = ByteArray(2048)
    val ret = mutableListOf<Byte>()
    while (!inflater.finished()){
        val size = inflater.inflate(buffer)
        ret.addAll(buffer.copyOf(size).toList())
    }
    inflater.end()
    return ret.toByteArray()
}

@OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
fun ByteArray.base64Encode() : String{
    return Base64.encode(this)
}

@OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
fun String.base64Decode() : ByteArray{
    return Base64.decode(this)
}

fun Any.binSerializationToSendThroughJson() : JsonElement{
    return this.serializeByKyro().deflate().base64Encode().toJsonElement()
}

fun JsonElement.binDeserializationFromJson() : Any{
    return this.jsonPrimitive.content.base64Decode().inflate().deserializeByKyro()
}

// from https://github.com/Kotlin/kotlinx.serialization/issues/746
fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Array<*> -> JsonArray(map { it.toJsonElement() })
    is List<*> -> JsonArray(map { it.toJsonElement() })
    is Map<*, *> -> JsonObject(map { it.key.toString() to it.value.toJsonElement() }.toMap())
    else -> Json.encodeToJsonElement(serializer(this::class.createType()), this)
}

fun JsonElement.toKotlinValues() : Any = when (this) {
    is JsonPrimitive -> {
        if (this.isString) this.content
        else if (this.booleanOrNull != null) this.boolean
        else if (this.intOrNull != null) this.int
        else if (this.longOrNull != null) this.long
        else if (this.floatOrNull != null) this.float
        else if (this.doubleOrNull != null) this.double
        else error("convertIfPrimitive failed")
    }
    is JsonObject -> this.toMutableMap().mapValues { it.value.toKotlinValues() }
    is JsonArray -> this.toList()
    else -> error("convertIfPrimitive failed")
}

fun Any?.toJsonString(): String = Json.encodeToString(this.toJsonElement())