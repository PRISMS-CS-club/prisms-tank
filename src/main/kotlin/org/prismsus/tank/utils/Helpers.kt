package org.prismsus.tank.utils

import org.prismsus.tank.utils.collidable.DPos2
import java.util.TreeSet
import kotlin.math.PI

fun Double.toDeg() : Double {
    // turn radians into degrees
    return this * 180.0 / PI
}

fun Double.toRad() : Double {
    return this * PI / 180.0
}

fun Double.toModPosAngle() : Double{
    var angle = this
    while (angle < 0){
        angle += 2 * PI
    }
    return angle % (2 * PI)
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