package org.prismsus.tank.utils

/**
 * A 2D vector with integer components.
 * @property x The x component.
 * @property y The y component.
 */
class IVec2(var x: Int, var y: Int) {
    constructor(): this(0, 0)
    constructor(vec: DVec2): this(vec.x.toInt(), vec.y.toInt())

    /**
     * Add this int vector and another int vector. Returns a new object.
     * @param other The other int vector.
     * @return The sum vector.
     */
    operator fun plus(other: IVec2): IVec2 {
        return IVec2(x + other.x, y + other.y)
    }
    operator fun plusAssign(other: IVec2) {
        x += other.x
        y += other.y
    }

    /**
     * Subtract this int vector and another int vector. Returns a new object.
     * @param other The other int vector.
     * @return The subtracted vector.
     */
    operator fun minus(other: IVec2): IVec2 {
        return IVec2(x - other.x, y - other.y)
    }

    operator fun minusAssign(other: IVec2) {
        x -= other.x
        y -= other.y
    }

    /**
     * Multiply this int vector and an integer scalar. Returns a new object.
     * @param other The scalar.
     * @return The product vector.
     */
    operator fun times(other: Int): IVec2 {
        return IVec2(x * other, y * other)
    }

    operator fun timesAssign(other: Int) {
        x *= other
        y *= other
    }

    /**
     * Multiply this int vector and a double scalar. Returns a new object.
     * @param other The scalar.
     */
    operator fun times(other: Double): DVec2 {
        return DVec2(x * other, y * other)
    }
    operator fun timesAssign(other: Double) {
        x *= other.toInt()
        y *= other.toInt()
    }

    /**
     * Divide this int vector and an integer scalar. Returns a new object.
     * @param other The scalar.
     * @return The divided vector.
     */
    operator fun div(other: Int): IVec2 {
        return IVec2(x / other, y / other)
    }

    operator fun divAssign(other: Int) {
        x /= other
        y /= other
    }

    /**
     * Divide this int vector and a double scalar. Returns a new object.
     * @param other The scalar.
     * @return The divided vector.
     */
    operator fun div(other: Double): DVec2 {
        return DVec2(x / other, y / other)
    }

    operator fun divAssign(other: Double) {
        x /= other.toInt()
        y /= other.toInt()
    }

    /**
     * Get the dot product of this int vector and another int vector.
     * @param other The other int vector.
     * @return The dot product.
     */
    fun dot(other: IVec2): Int {
        return x * other.x + y * other.y
    }

    /**
     * Get the cross product of this int vector and another int vector.
     * @param other The other int vector.
     * @return The cross product.
     */
    fun cross(other: IVec2): Int {
        return x * other.y - y * other.x
    }

    fun toDvec2(): DVec2 {
        return DVec2(x.toDouble(), y.toDouble())
    }
}