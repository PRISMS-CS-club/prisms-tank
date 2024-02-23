package org.prismsus.tank.utils

import java.util.TreeSet
import kotlin.math.PI
import kotlin.random.Random
import com.esotericsoftware.kryo.io.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

fun Double.toDeg() : Double {
    // turn radians into degrees
    return this * 180.0 / PI
}

fun Double.toRad() : Double {
    return this * PI / 180.0
}

// to positive angle in radians
fun Double.toModPosAngle() : Double{
    var angle = this
    angle %= 2 * PI
    if (angle < 0) angle += 2 * PI
    return angle
}

fun Double.toModAngle() : Double {
    var angle = this
    angle %= 2 * PI
    return if (angle > PI) angle - 2 * PI else angle
}

fun Double.toFixed(digit: Int) : FixedPoint {
    return FixedPoint(this, digit)
}

fun Double.toEvtFixed() : FixedPoint {
    return FixedPoint(this, EVT_NUM_DIGIT)
}

fun Float.toFixed(digit: Int) : FixedPoint {
    return FixedPoint(this.toDouble(), digit)
}

fun Float.toEvtFixed() : FixedPoint {
    return FixedPoint(this.toDouble(), EVT_NUM_DIGIT)
}

fun Float.toTimeFixed() : FixedPoint {
    return FixedPoint(this.toDouble(), TIME_DIGIT)
}

fun Double.toTimeFixed() : FixedPoint {
    return FixedPoint(this, TIME_DIGIT)
}

fun Number.toEvtFixed() : Number {
    return when(this) {
        is Double -> this.toEvtFixed()
        is Float -> this.toEvtFixed()
        else -> this
    }
}

fun Number.toTimeFixed() : Number {
    return when(this) {
        is Double -> this.toTimeFixed()
        is Float -> this.toTimeFixed()
        else -> this
    }
}

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

infix fun Double.errEQ(other : Double) : Boolean {
    return Math.abs(this - other) < DOUBLE_PRECISION
}

infix fun Double.errNE(other : Double) : Boolean {
    return Math.abs(this - other) >= DOUBLE_PRECISION
}


public fun <T> Iterable<T>.treeDistinct() : List<T> {
    val trSet = TreeSet<T>()
    for (it in this){
        trSet.add(it)
    }
    return trSet.toList()
}

public fun <T> Array<out T>.treeDistinct() : List<T> {
    val trSet = TreeSet<T>()
    for (it in this){
        trSet.add(it)
    }
    return trSet.toList()
}

public fun <T> Iterable<T>.treeDistinctBy(selector: (T) -> Comparable<*>?) : List<T> {
    val trSet = TreeSet<T>(compareBy(selector))
    for (it in this){
        trSet.add(it)
    }
    return trSet.toList()
}

public fun <T> Array<out T>.treeDistinctBy(selector: (T) -> Comparable<*>?) : List<T> {
    val trSet = TreeSet<T>(compareBy(selector))
    for (it in this){
        trSet.add(it)
    }
    return trSet.toList()
}

fun Int.toXvec() : IVec2 {
    return IVec2(this, 0)
}

fun Int.toYvec() : IVec2 {
    return IVec2(0, this)
}

operator fun Int.times(other: IVec2): IVec2 {
    return IVec2(this * other.x, this * other.y)
}

fun Double.toXvec() : DVec2 {
    return DVec2(this, 0.0)
}

fun Double.toYvec() : DVec2 {
    return DVec2(0.0, this)
}

operator fun Double.times(other: DVec2): DVec2 {
    return DVec2(this * other.x, this * other.y)
}


fun<E> Collection<E>.randomSelect(distribution : Array<out Number>) : E{
    val total = distribution.sumOf { it.toDouble() }
    val rand = Math.random() * total
    var psum = 0.0
    for ((i, it) in this.withIndex()){
        psum += distribution[i].toDouble()
        if (psum >= rand) return it
    }
    error("randomSelect failed")
}

fun Number.compareTo(other : Number) : Int {
    return when {
        this.toDouble() > other.toDouble() -> 1
        this.toDouble() < other.toDouble() -> -1
        else -> 0
    }
}

abstract  class CompNum(open val num : Number) : Comparable<CompNum>, Number(){
}

data class CompInt(override val num : Int) : CompNum(num){
    override fun compareTo(other: CompNum) = num.compareTo(other)
    override fun toByte() = num.toByte()
    override fun toChar() = num.toChar()
    override fun toDouble() = num.toDouble()
    override fun toFloat() = num.toFloat()
    override fun toInt() = num
    override fun toLong() = num.toLong()
    override fun toShort() = num.toShort()
    override fun toString() = num.toString()

    operator fun plus(other : CompInt) = CompInt(num + other.num)
    operator fun minus(other : CompInt) = CompInt(num - other.num)
    operator fun unaryMinus() = CompInt(-num)
    operator fun unaryPlus() = CompInt(num)
    operator fun times(other : CompInt) = CompInt(num * other.num)
    operator fun div(other : CompInt) = CompInt(num / other.num)
    operator fun rem(other : CompInt) = CompInt(num % other.num)
    operator fun plus(other : CompDouble) = CompDouble(num + other.num)

    operator fun minus(other : CompDouble) = CompDouble(num - other.num)
    operator fun times(other : CompDouble) = CompDouble(num * other.num)
    operator fun div(other : CompDouble) = CompDouble(num / other.num)
    operator fun rem(other : CompDouble) = CompDouble(num % other.num)
}

data class CompDouble(override val num : Double) : CompNum(num){
    override fun compareTo(other: CompNum) = num.compareTo(other)
    override fun toByte() = num.toInt().toByte()
    override fun toChar() = num.toChar()
    override fun toDouble() = num
    override fun toFloat() = num.toFloat()
    override fun toInt() = num.toInt()
    override fun toLong() = num.toLong()
    override fun toShort() = num.toInt().toShort()
    override fun toString() = num.toString()


    operator fun minus(other : CompDouble) = CompDouble(num - other.num)
    operator fun times(other : CompDouble) = CompDouble(num * other.num)
    operator fun div(other : CompDouble) = CompDouble(num / other.num)
    operator fun rem(other : CompDouble) = CompDouble(num % other.num)
    operator fun unaryMinus() = CompDouble(-num)
    operator fun unaryPlus() = CompDouble(num)

    operator fun plus(other : CompInt) = CompDouble(num + other.num)
    operator fun minus(other : CompInt) = CompDouble(num - other.num)
    operator fun times(other : CompInt) = CompDouble(num * other.num)
    operator fun div(other : CompInt) = CompDouble(num / other.num)
    operator fun rem(other : CompInt) = CompDouble(num % other.num)
}


fun Int.toComp() : CompInt{
    return CompInt(this)
}

fun Double.toComp() : CompDouble{
    return CompDouble(this)
}

fun Number.toComp() : CompNum{
    return when(this){
        is Int -> CompInt(this)
        is Double -> CompDouble(this)
        else -> error("toComp failed")
    }
}

fun ClosedRange<CompInt>.genRand() : Int{
    val rand = Random.nextInt(start.num, endInclusive.num)
    return rand
}

fun ClosedRange<CompDouble>.genRand() : Double{
    val rand = Random.nextDouble(start.num, endInclusive.num)
    return rand
}

fun ClosedRange<out CompNum>.genRand() : Number{
    if (this.start is CompDouble){
        return (this as ClosedRange<CompDouble>).genRand()
    }
    if (this.start is CompInt){
        return (this as ClosedRange<CompInt>).genRand()
    }
    error("genRand failed")
}

fun<T> T.deepCopyByKyro() : T{
    return thSafeKyro.copy(this)
}

fun<T> T.serializeByKyro() : ByteArray{
    val out = Output(1024, -1)
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

//fun <T> ByteArray.deserializeByKyro(clazz: Class<T>): T {
//    val input = Input(this)
//    val ret = thSafeKyro.readClassAndObject(input)
//    input.close()
//    return ret as T
//}

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


fun<T> lazyExtended(initializer : () -> T) = LazyExtended(initializer)
class LazyExtended<T>(val initializer : () -> T) : Lazy<T>{
    // enable reset and replace of the value
    private var curLazy = lazy(initializer)
    private var _value : T? = null
    override val value : T get() = if (_value == null) curLazy.value!! else _value!!
    override fun isInitialized() = curLazy.isInitialized() || (_value != null)
    override fun toString() = if (_value == null) curLazy.toString() else _value.toString()
     fun reset() = run { _value = null; curLazy = lazy(initializer) }
    fun replace(newValue : T) {
        _value = newValue
    }
}


fun<T> lazyThreadLocal(initializer : () -> T) = LazyThreadLocal(lazy(initializer))
fun<T> lazyExThreadLocal(initializer : () -> T) = LazyThreadLocal(lazyExtended(initializer))

class LazyThreadLocal<T>(val wrap : Lazy<T>) : Lazy<T>{
    val thLocal = ThreadLocal.withInitial { wrap }
    override val value: T by thLocal.get()
    override fun isInitialized() = thLocal.get().isInitialized()
}

inline fun <reified T : Any> Any.getMemberByName(propertyName: String): T {
    // https://stackoverflow.com/a/35525706/20080946
    val getterName = "get" + propertyName.replaceFirstChar { it.uppercase()  }
    return javaClass.getMethod(getterName).invoke(this) as T
}

fun Any.setPropertyByName(propertyName : String, value : Any){
    // this works even for private and val properties
    val declaredField = getPropertyByName(propertyName).javaField!!
    val origAccessibility = declaredField.isAccessible
    declaredField.isAccessible = true
    declaredField.set(this, value)
    declaredField.isAccessible = origAccessibility
}

inline fun <reified T : Any> T.getPropertyByName(propertyName : String) : KProperty1<out T, *> {
    return this::class.memberProperties.find { it.name == propertyName }!!
}