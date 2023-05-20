package org.prismsus.tank.utils

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
        angle += 360
    }
    return angle % 360
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