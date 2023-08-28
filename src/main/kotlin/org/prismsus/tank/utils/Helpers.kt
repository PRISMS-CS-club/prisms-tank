package org.prismsus.tank.utils

import org.prismsus.tank.utils.collidable.DPos2
import java.util.Collections
import java.util.TreeSet
import kotlin.math.PI
import kotlin.random.Random
import com.esotericsoftware.kryo.*
import com.esotericsoftware.kryo.io.*
import io.ktor.server.netty.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import kotlin.reflect.full.createType

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
    return thSafeKyro.get().copy(this)
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

fun Any?.toJsonString(): String = Json.encodeToString(this.toJsonElement())


fun<T> lazyExtended(initializer : () -> T) = LazyExtended(initializer)
class LazyExtended<T>(val initializer : () -> T) : Lazy<T>{
    private var curLazy = lazy(initializer)
    private var _value : T? = null
    override val value = if (_value == null) curLazy.value else _value!!
    override fun isInitialized() = curLazy.isInitialized() || (value != null)
    override fun toString() = if (value == null) curLazy.toString() else _value.toString()
     fun reset() = run { _value = null; curLazy = lazy(initializer) }
    fun replace(newValue : T) {
        _value = newValue
    }
}