package org.prismsus.tank.utils

import org.prismsus.tank.utils.collidable.DPos2
import kotlin.math.*
import kotlin.random.*

/**
 * A 2D vector with double coordinates.
 * @property x The x coordinate.
 * @property y The y coordinate.
 */
data class DVec2(var x: Double, var y: Double) {
    constructor() : this(0.0, 0.0)
    constructor(vec: IVec2) : this(vec.x.toDouble(), vec.y.toDouble())
    constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())
    init{
        assert(!x.isNaN() && !y.isNaN()){
            "DVec2: NaN"
        }
    }
    /**
     * Add this double vector and another double vector. Returns a new object.
     * @param other The other double vector.
     * @return The sum vector.
     */
    operator fun plus(other: DVec2): DVec2 {
        return DVec2(x + other.x, y + other.y)
    }

    operator fun plus(other: DPos2): DPos2 {
        return DPos2(x + other.x, y + other.y)
    }

    operator fun plusAssign(other: DVec2) {
        x += other.x
        y += other.y
    }

    /**
     * Subtract this double vector and another double vector. Returns a new object.
     * @param other The other double vector.
     * @return The subtracted vector.
     */
    operator fun minus(other: DVec2): DVec2 {
        return DVec2(x - other.x, y - other.y)
    }

    operator fun minus(other: DPos2): DPos2 {
        return DPos2(x - other.x, y - other.y)
    }

    operator fun minusAssign(other: DVec2) {
        x -= other.x
        y -= other.y
    }

    operator fun unaryMinus(): DVec2 {
        return DVec2(-x, -y)
    }

    operator fun unaryPlus(): DVec2 {
        return DVec2(x, y)
    }


    /**
     * Multiply this double vector and a scalar. Returns a new object.
     * @param other The scalar.
     * @return The product vector.
     */
    operator fun times(other: Double): DVec2 {
        return DVec2(x * other, y * other)
    }

    operator fun timesAssign(other: Double) {
        x *= other
        y *= other
    }

    /**
     * Divide this double vector and a scalar. Returns a new object.
     * @param other The scalar.
     * @return The divided vector.
     */
    operator fun div(other: Double): DVec2 {
        return DVec2(x / other, y / other)
    }

    operator fun divAssign(other: Double) {
        x /= other
        y /= other
    }

    override fun equals(other: Any?): Boolean {
        if (other is DVec2)
            return abs(x - other.x) < DOUBLE_PRECISION && abs(y - other.y) < DOUBLE_PRECISION
        if (other is IVec2)
            return abs(x - other.x) < DOUBLE_PRECISION && abs(y - other.y) < DOUBLE_PRECISION
        return false
    }

    /**
     * Get the length of the vector. Consider it is a line extending from the origin.
     * @return The length of the vector.
     */
    fun len(): Double {
        return sqrt(x * x + y * y)
    }

    /**
     * Get the square of the length of the vector. Consider it is a line extending from the origin.
     * @return The square of the length of the vector.
     * @see len
     * */
    fun sqLen(): Double {
        return x * x + y * y
    }

    /**
     * Get the normalized vector. (meaning the length of the vector is 1, but the direction is the same)
     * @return The normalized vector.
     */
    fun norm(): DVec2 {
        return this / len()
    }

    /**
     * Get the dot product of this vector and another vector.
     * @param other The other vector.
     * @return The dot product.
     */
    infix fun dot(other: DVec2): Double {
        return x * other.x + y * other.y
    }

    /**
     * Calculate the cosine of the angle between this vector and another vector. The angle is always the smaller one.
     * @param other The other vector.
     * @return The cosine of the angle.
     * @see angleWith
     * */
    infix fun cosWith(other: DVec2): Double {
        return dot(other) / (len() * other.len())
    }

    /**
     * Calculate the angle between this vector and another vector. The angle is always the smaller one.
     * @param other The other vector.
     * @return The angle in radian.
     * @see cosWith
     * */

    infix fun angleWith(other: DVec2): Double {
        return acos(cosWith(other))
    }


    /**
     * Get the cross product of this vector and another vector.
     * @param other The other vector.
     * @return The cross product.
     */
    fun cross(other: DVec2): Double {
        return x * other.y - y * other.x
    }

    /**
     * Round the vector to the nearest integer vector.
     * @return The rounded vector.
     */
    fun round(): IVec2 {
        return IVec2(x.roundToInt(), y.roundToInt())
    }

    /**
     * Rotate the angle to the given angle
     * @param angle The angle in radiance to rotate to.
     * @return The rotated vector.
     */

    fun rotateTo(rad: Double): DVec2 {
        val len = len()
        val nx = len * cos(rad)
        val ny = len * sin(rad)
        return DVec2(nx, ny)
    }

    /**
     * Get the angle of the vector.
     * @return The angle of the vector.
     */

    fun angle(): Double {
        return atan2(y, x)
    }


    /**
     * Rotate the vector using center = (0, 0), in radians.
     * @param radOffset The angle in radian to turn
     * @return The turned vector.
     */
    fun rotate(radOffset: Double): DVec2 {
        val curAngle = angle()
        val newAngle = curAngle + radOffset
        return rotateTo(newAngle)
    }

    /**
     * Rotate the vector according to certain center or pivot, in radians.
     * @param center The center or pivot to rotate around.
     * @param radOffset The angle in radian to turn, in radian.
     * @return The turned vector.
     *
     * */
    fun rotate(radOffset: Double, center: DVec2): DVec2 {
        val curAngle = angle()
        val newAngle = curAngle + radOffset
        val toThis = this - center // from the center to here
        val rotated = toThis.rotateTo(newAngle)
        return rotated + center
    }

    /**
     * Rotate the vector according to certain center or pivot, in degrees.
     * @param center The center or pivot to rotate around.
     * @param degOffset The angle in degrees to turn, in degrees.
     * @return The turned vector.
     * @see rotate
     * */
    fun rotateDeg(degOffset: Double): DVec2 {
        return rotate(degOffset.toRad())
    }

    /**
     * Call rotate, then assign the result to this vector, angle represented in radians. Using center = (0, 0).
     * @param radOffset The angle in radian to turn, in radian.
     * @return This vector.
     * @see rotate
     * */
    fun rotateAssign(radOffset: Double): DVec2 {
        val curAngle = angle()
        val newAngle = curAngle + radOffset
        val len = len()
        x = len * cos(newAngle)
        y = len * sin(newAngle)
        return this
    }

    /**
     * Call rotate, then assign the result to this vector, angle represented in radians. Need to specify the center or pivot.
     * @param center The center or pivot to rotate around
     * @param radOffset The angle in radian to turn, in radian.
     * @return This vector.
     * */
    fun rotateAssign(radOffset: Double, center: DVec2): DVec2 {
        val curAngle = angle()
        val newAngle = curAngle + radOffset
        val toThis = this - center // from the center to here
        val rotated = toThis.rotateTo(newAngle)
        x = rotated.x + center.x
        y = rotated.y + center.y
        return this
    }


    override fun toString(): String {
        return "($x, $y)"
    }

    /**
     *  to get the perpendicular vector of this vector, at its left hand side.
     *  @return the perpendicular vector of this vector, at its left hand side.
     * */
    fun leftHandPerp(): DVec2 {
        return DVec2(-y, x)
    }

    /**
     * to get the perpendicular vector of this vector, at its right hand side.
     * @return the perpendicular vector of this vector, at its right hand side.
     * */
    fun rightHandPerp(): DVec2 {
        return DVec2(y, -x)
    }

    fun isPtAtLeft(pt: DPos2): Boolean {
        return leftHandPerp().dot((pt - this).toVec()) >= 0
    }

    fun isPtAtRight(pt: DPos2): Boolean {
        return rightHandPerp().dot((pt - this).toVec()) >= 0
    }

    fun toPt(): DPos2 {
        return DPos2(x, y)
    }

    fun abs() : DVec2 {
        return DVec2(abs(x), abs(y))
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    val xVec: DVec2
        get() {
            return DVec2(x, 0.0)
        }

    val yVec: DVec2
        get() {
            return DVec2(0.0, y)
        }

    companion object {


        /**
         * create a vector from angle and length
         * @param angle in rad
         * @param length the length of the vector
         * @return the vector created
         */
        fun byPolar(len: Double, rad: Double): DVec2 {
            return DVec2(len * cos(rad), len * sin(rad))
        }

        /**
         * Randomly generate a unit vector.
         * @return The generated vector.
         * */
        fun randUnitVec(): DVec2 {
            val rad = Random.nextDouble() * 2 * PI
            return DVec2(cos(rad), sin(rad))
        }


        val ORIGIN: DVec2 get() = DVec2(0.0, 0.0)
        val UP: DVec2 get() = DVec2(0.0, 1.0)
        val DN: DVec2 get() = -UP
        val RT: DVec2 get() = DVec2(1.0, 0.0)
        val LF: DVec2 get() = -RT
        val RTS_DIR = arrayOf(UP, RT, DN, LF) // directions by turning right
        val LFS_DIR = arrayOf(UP, LF, DN, RT) // directions by turning left
    }

}
typealias DDim2 = DVec2