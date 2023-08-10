package org.prismsus.tank.utils

import org.prismsus.tank.utils.collidable.DPos2
import java.util.Collections
import java.util.TreeSet
import kotlin.math.PI
import kotlin.random.Random
import com.esotericsoftware.kryo.*
import com.esotericsoftware.kryo.io.*

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

abstract class CompNum(open val num : Number) : Comparable<CompNum>, Number(){
}

class CompInt(override val num : Int) : CompNum(num){
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

class CompDouble(override val num : Double) : CompNum(num){
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