package org.prismsus.tank.utils

import kotlin.math.PI

fun Double.toDeg() : Double {
    // turn radians into degrees
    return this * 180.0 / PI
}

fun Double.toRad() : Double {
    return this * PI / 180.0
}