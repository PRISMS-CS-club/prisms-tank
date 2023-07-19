package org.prismsus.tank.utils

import org.prismsus.tank.utils.collidable.DPos2

/**
 * A 2D vector with integer components.
 * @property x The x component.
 * @property y The y component.
 */
data class IVec2(var x: Int, var y: Int) {
    constructor(): this(0, 0)
    constructor(vec: DVec2): this(vec.x.toInt(), vec.y.toInt())

    /**
     * Get the negation of this vector.
     */
    operator fun unaryMinus(): IVec2 {
        return IVec2(-x, -y)
    }

    operator fun unaryPlus(): IVec2 {
        return this
    }

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

    fun toDVec2(): DVec2 {
        return DVec2(x.toDouble(), y.toDouble())
    }

    fun toDPos2(): DPos2 {
        return DPos2(x.toDouble(), y.toDouble())
    }

    override fun toString(): String {
        return "($x, $y)"
    }

    override fun equals(other: Any?): Boolean {
        if (other is IVec2)
            return x == other.x && y == other.y
        else if (other is DVec2)
            return other.equals(this)
        return false
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    companion object {
        val ORIGIN = IVec2(0, 0)
        val UP = IVec2(0, 1)
        val DN = IVec2(0, -1)
        val LF = IVec2(-1, 0)
        val RT = IVec2(1, 0)
    }
}
typealias IPos2 = IVec2
typealias IDim2 = IVec2
