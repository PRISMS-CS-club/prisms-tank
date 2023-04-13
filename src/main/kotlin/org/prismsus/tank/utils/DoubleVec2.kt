package org.prismsus.tank.utils

import kotlin.math.roundToInt
import kotlin.math.sqrt

class DoubleVec2(var x: Double, var y: Double) {
    constructor(): this(0.0, 0.0)
    constructor(vec: IntVec2): this(vec.x.toDouble(), vec.y.toDouble())

    /**
     * Add this double vector and another double vector. Returns a new object.
     * @param other The other double vector.
     * @return The sum vector.
     */
    operator fun plus(other: DoubleVec2): DoubleVec2 {
        return DoubleVec2(x + other.x, y + other.y)
    }

    /**
     * Subtract this double vector and another double vector. Returns a new object.
     * @param other The other double vector.
     * @return The subtracted vector.
     */
    operator fun minus(other: DoubleVec2): DoubleVec2 {
        return DoubleVec2(x - other.x, y - other.y)
    }

    /**
     * Multiply this double vector and a scalar. Returns a new object.
     * @param other The scalar.
     * @return The product vector.
     */
    operator fun times(other: Double): DoubleVec2 {
        return DoubleVec2(x * other, y * other)
    }

    /**
     * Divide this double vector and a scalar. Returns a new object.
     * @param other The scalar.
     * @return The divided vector.
     */
    operator fun div(other: Double): DoubleVec2 {
        return DoubleVec2(x / other, y / other)
    }

    /**
     * Get the length of the vector.
     * @return The length of the vector.
     */
    fun length(): Double {
        return sqrt(x * x + y * y)
    }

    /**
     * Get the normalized vector.
     * @return The normalized vector.
     */
    fun normalized(): DoubleVec2 {
        return this / length()
    }

    /**
     * Get the dot product of this vector and another vector.
     * @param other The other vector.
     * @return The dot product.
     */
    fun dot(other: DoubleVec2): Double {
        return x * other.x + y * other.y
    }

    /**
     * Get the cross product of this vector and another vector.
     * @param other The other vector.
     * @return The cross product.
     */
    fun cross(other: DoubleVec2): Double {
        return x * other.y - y * other.x
    }

    /**
     * Round the vector to the nearest integer vector.
     * @return The rounded vector.
     */
    fun round(): IntVec2 {
        return IntVec2(x.roundToInt(), y.roundToInt())
    }
}