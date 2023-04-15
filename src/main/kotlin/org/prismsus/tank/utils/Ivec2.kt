package org.prismsus.tank.utils

class Ivec2(var x: Int, var y: Int) {
    constructor(): this(0, 0)
    constructor(vec: Dvec2): this(vec.x.toInt(), vec.y.toInt())

    /**
     * Add this int vector and another int vector. Returns a new object.
     * @param other The other int vector.
     * @return The sum vector.
     */
    operator fun plus(other: Ivec2): Ivec2 {
        return Ivec2(x + other.x, y + other.y)
    }

    /**
     * Subtract this int vector and another int vector. Returns a new object.
     * @param other The other int vector.
     * @return The subtracted vector.
     */
    operator fun minus(other: Ivec2): Ivec2 {
        return Ivec2(x - other.x, y - other.y)
    }

    /**
     * Multiply this int vector and an integer scalar. Returns a new object.
     * @param other The scalar.
     * @return The product vector.
     */
    operator fun times(other: Int): Ivec2 {
        return Ivec2(x * other, y * other)
    }

    /**
     * Multiply this int vector and a double scalar. Returns a new object.
     * @param other The scalar.
     */
    operator fun times(other: Double): Dvec2 {
        return Dvec2(x * other, y * other)
    }

    /**
     * Divide this int vector and an integer scalar. Returns a new object.
     * @param other The scalar.
     * @return The divided vector.
     */
    operator fun div(other: Int): Ivec2 {
        return Ivec2(x / other, y / other)
    }

    /**
     * Divide this int vector and a double scalar. Returns a new object.
     * @param other The scalar.
     * @return The divided vector.
     */
    operator fun div(other: Double): Dvec2 {
        return Dvec2(x / other, y / other)
    }

    /**
     * Get the dot product of this int vector and another int vector.
     * @param other The other int vector.
     * @return The dot product.
     */
    fun dot(other: Ivec2): Int {
        return x * other.x + y * other.y
    }

    /**
     * Get the cross product of this int vector and another int vector.
     * @param other The other int vector.
     * @return The cross product.
     */
    fun cross(other: Ivec2): Int {
        return x * other.y - y * other.x
    }
}