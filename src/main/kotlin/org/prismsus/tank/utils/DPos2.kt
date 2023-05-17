package org.prismsus.tank.utils
import kotlin.math.*
class DPos2(var x: Double, var y: Double) : Intersectable, Comparable<DPos2>, Cloneable {

    val origX = x
    val origY = y

    constructor(other : DPos2) : this(other.x, other.y)
    override operator fun plus(other: DVec2): DPos2 {
        return DPos2(x + other.x, y + other.y)
    }

    operator fun plus(other: DPos2): DVec2 {
        return DVec2(x + other.x, y + other.y)
    }

    override operator fun minus(other: DVec2): DPos2 {
        return DPos2(x - other.x, y - other.y)
    }

    operator fun minus(other: DPos2): DVec2 {
        return DVec2(x - other.x, y - other.y)
    }

    operator fun unaryMinus(): DPos2 {
        return DPos2(-x, -y)
    }

    operator fun unaryPlus(): DPos2 {
        return DPos2(x, y)
    }
    override var angleRotated: Double
        get() = 0.0
        set(value) {}
    override val unrotated: Intersectable
        get() = DPos2(origX, origY)

    override var pts: Array<DPos2>
        get() = arrayOf(DPos2(x, y))
        set(value) {
            x = value[0].x
            y = value[0].y
        }


    override var rotationCenter: DPos2
        get() = this
        set(value) {super.rotationCenter = value}
    override var encSquareSize: DDim2 // this is a point, so the size is 0
        get() = DDim2(0.0, 0.0)
        set(value) {}

    override operator fun compareTo(other: DPos2): Int {
        // compare x first, then y
        if (abs(x - other.x) > DOUBLE_PRECISION){
            return x.compareTo(other.x)
        }
        if (abs(y - other.y) < DOUBLE_PRECISION) {
            return 0
        }
        return y.compareTo(other.y)
    }

    /**
     * Check if two intersectable objects intersect.
     * @see [Intersectable.intersect]
     * */
    override fun intersect(other: Intersectable): Boolean {
        if (other is DPos2)
            return this == other
        return other.intersect(this)
        // except ColBox classes, classes only handle intersects with same type
    }

    /**
     * Represent DVec to some point in 2D space, and calculate the distance between this and other point.
     * @param other The other point.
     * @return The distance between this and other point.
     * */
    fun dis(other: DPos2): Double {
        return sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
    }

    /**
     * Represent DVec to some point in 2D space, and calculate the square of the distance between this and other point.
     * @param other The other point.
     * @return The square of the distance between this and other point.
     * @see dis
     * */
    fun sqDis(other: DPos2): Double {
        return (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)
    }

    /**
     * Represent DVec to some point in 2D space, and calculate the Manhattan distance between this and other point.
     * Manhattan distance is the sum of the absolute values of the differences of the x and y coordinates.
     * This distance the distance between two points when limiting the movement to only horizontal and vertical.
     * @param other The other point.
     * @return The Manhattan distance between this and other point.
     * */
    fun manhatDis(other: DPos2): Double {
        return abs(x - other.x) + abs(y - other.y)
    }

    fun min(other: DPos2): DPos2 {
        return DPos2(min(x, other.x), min(y, other.y))
    }

    fun max(other: DPos2): DPos2 {
        return DPos2(max(x, other.x), max(y, other.y))
    }

    override fun byPts(pts: Array<DPos2>): Intersectable {
        return DPos2(x, y)
    }

    fun toVec(): DVec2{
        return DVec2(x, y)
    }


    override fun rotateAssign(radOffset: Double, center: DPos2): Intersectable {
        // other intersectables are made of points, and they call the rotateAssign in point class
        // so we need to implement the rotateAssign in point class as base of other intersectables
        val vec = this - center
        val rotated = vec.rotate(radOffset)
        x = rotated.x + center.x
        y = rotated.y + center.y
        return this
    }

    override fun rotate(rad: Double, center: DPos2): DPos2 {
        val vec = this - center
        val rotated = vec.rotate(rad)
        return DPos2(rotated.x + center.x, rotated.y + center.y)
    }

    override fun toString(): String {
        return "($x, $y)"
    }

    override fun equals(other: Any?) : Boolean {
        if (other !is DPos2)
            return false
        return abs(x - other.x) < DOUBLE_PRECISION && abs(y - other.y) < DOUBLE_PRECISION
    }

    override fun copy(): DPos2 {
        return DPos2(this)
    }

    companion object{
        val ORIGIN = DPos2(0.0, 0.0)
        val UP     = DPos2(0.0, 1.0)
        val DN     = -(UP.toVec()).toPt()
        val RT     = DPos2(1.0, 0.0)
        val LF     = -(RT.toVec()).toPt()
        val RTS_DIR = arrayOf(UP, RT, DN, LF) // directions by turning right
        val LFS_DIR = arrayOf(UP, LF, DN, RT) // directions by turning left
    }
}