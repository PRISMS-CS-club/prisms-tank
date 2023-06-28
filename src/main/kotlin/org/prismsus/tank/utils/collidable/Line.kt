package org.prismsus.tank.utils.collidable
import org.prismsus.tank.utils.DOUBLE_PRECISION
import org.prismsus.tank.utils.DVec2
import org.prismsus.tank.utils.errEQ
import org.prismsus.tank.utils.errNE
import java.awt.Shape
import java.awt.geom.Line2D
import kotlin.math.*
import javax.swing.*

/**
 * @property startP The starting point of the line.
 * @property endP The ending point of the line.
 * @constructor Create a line with two points.
 * */
class Line(override var pts : Array<DPos2>) : Collidable, Comparable<Line> {
    init{
        if (pts.size != 2) {
            throw IllegalArgumentException("Line must be initialized with two points")
        }
    }

    override var angleRotated = 0.0

    override var rotationCenter: DPos2
        get() = ((startP.toVec() + endP.toVec()) / 2.0).toPt()
        set(value) {}


    constructor (startP : DPos2, endP : DPos2) : this(arrayOf(startP, endP)){

    }
    var slope
        get() = (endP.y - startP.y) / (endP.x - startP.x)
        set(value) {}
    var inter
        get() = startP.y - slope * startP.x
        set(value) {}
    var startP: DPos2
        get() = pts[0]
        set(new) {
            pts[0] = new
        }
    var endP: DPos2
        get() = pts[1]
        set(new) {
            pts[1] = new
        }

    /**
     * Check if two intersectable objects intersect.
     * @param other The other object.
     * @return True if intersects, false otherwise.
     * @see Collidable.collide
     * */
    override fun intersectPts(other: Collidable): Array<DPos2> {
        if (other is DPos2) {
            if (isVerti()){
                if (other.x errEQ startP.x && inYrg(other.y))
                    return arrayOf(other)
                return arrayOf()
            }
            // calculate y position of the line given x is other.x
            val y = slope * other.x + inter
            if (inXrg(other.x) && y errEQ other.y)
                return arrayOf(other)
            return emptyArray()
        }
        if (other !is Line) {
            // in this case, the other object is a colPoly
            return other.collidePts(this)
        }
        val otherLine = other as Line
        // check if two lines are parallel, in this case, they will never intersect
        if (slope errEQ otherLine.slope && ! isVerti() && !otherLine.isVerti()) {
            if (inter errNE otherLine.inter) {
                return arrayOf()
            }
            val newStartX = max(startP.x, otherLine.startP.x)
            val newEndX = min(endP.x, otherLine.endP.x)
            if (newStartX > newEndX)
                return arrayOf()
            if (newStartX errEQ newEndX)
                return arrayOf(atX(newStartX))
            return arrayOf(atX(newStartX), atX(newEndX))
        }

        // check if the slope is infinity
        val infCnt: Int = (if (isVerti()) 1 else 0) +
                (if (other.isVerti()) 1 else 0)

        if (infCnt == 2) {
            // both lines are vertical
            if (startP.x errNE otherLine.startP.x) {
                return arrayOf()
            }
            val newStartY = max(startP.y, otherLine.startP.y)
            val newEndY = min(endP.y, otherLine.endP.y)
            if (newStartY > newEndY)
                return arrayOf()
            if (newStartY errEQ newEndY)
                return arrayOf(atY(newStartY))
            return arrayOf(atY(newStartY), atY(newEndY))
        }
        if (infCnt == 1) {

            // if only one of two lines are vertical
            // find the y value of the non-vertical line that meet with the vertical line
            val vLine =
                if (isVerti()) this else otherLine
            val nvLine =
                if (isVerti()) otherLine else this
            if (! nvLine.inXrg(vLine.startP.x)){
                return arrayOf()
            }
            val y = nvLine.atX(vLine.startP.x).y
            if (! nvLine.inYrg(y) ||!vLine.inYrg(y)) {
                return arrayOf()
            }
            return arrayOf(DPos2(vLine.startP.x, y))
        }

        val intersectX = (otherLine.inter - inter) / (slope - otherLine.slope)
        // calculate the point where intersection happens
        // then check if this point is in the range of both lines
        if (! inXrg(intersectX) || ! otherLine.inXrg(intersectX)) {
            return arrayOf()
        }
        return arrayOf(atX(intersectX))
    }

    override infix fun collidePts(other : Collidable) : Array<DPos2> {
        return intersectPts(other)
    }

    override infix fun enclosedPts(other: Collidable): Array<DPos2> {
        return emptyArray()
    }


    /**
     * Check if two lines are equal, which means they have the same starting point and ending point.
     * Notice that when the difference between two double values is less than [DOUBLE_PRECISION], they are considered equal.
     * @param other the other line to compare with
     * @return true if two lines are equal, false otherwise
     * @see DOUBLE_PRECISION
     * @see DPos2.equals
     * */
    override fun equals(other: Any?): Boolean {
        if (other !is Line) {
            return false
        }
        return startP == other.startP && endP == other.endP
    }

    override fun toString(): String {
        return "$startP -> $endP, slope: $slope, intercept: $inter"
    }


    /**
     * @return the length of the line
     * */
    fun len(): Double {
        return startP.dis(endP)
    }

    /**
     * @return the square of the length of the line
     * @see len
     * */
    fun sqLen(): Double {
        return startP.sqDis(endP)
    }

    /**
     * Return the point when travel a certain distance from the start point of the line
     * @param t the parameter of the line, t = 0 means the start point, t = 1 means the end point
     * @return the point when travel a certain distance from the start point of the line
     * */
    fun atT(t : Double) : DPos2 {
        return startP + (endP - startP) * t
    }

    /**
    * inverse of atT
    * */
    fun tOf(pt : DPos2) : Double {
        return (pt - startP).dot(endP - startP) / sqLen()
    }

    fun atLen(len : Double) : DPos2 {
        return startP + (endP - startP).norm() * len
    }

    fun atX(x : Double) : DPos2 {
        return DPos2(x, slope * x + inter)
    }

    fun atY(y : Double) : DPos2 {
        return DPos2((y - inter) / slope, y)
    }

    /**
     * Calculate the shortest distance from the line to the point
     * @param pt the point to calculate the distance to
     * @return the shortest distance from the line to the point
     * */
    fun disToPt(pt: DPos2): Double {
        // shortest distance from the line to the point
        if (len() < DOUBLE_PRECISION) return startP.dis(pt)
        val toPt = pt - startP // from the start point of the line to the point
        val t = toPt.dot(endP - startP) / sqLen()
        // first divided by Len(), meaning the projection of toPt on the line
        // divide another Len() to get the parameter t
        // clamp this t to make sure that it falls on the line segment
        val clampedT = t.coerceIn(0.0, 1.0)
        return atT(clampedT).dis(pt)
    }

    /**
     * Convert the line to a vector points from the starting point to the ending point
     * @return the vector representation of the line
     * */
    fun toVec(): DVec2 {
        return endP - startP
    }

    /**
     * Check if an x value is between the starting point and the ending point of the line, inclusively
     * @param x the x value to check
     * @return true if the x value is between the starting point and the ending point of the line, false otherwise
     * @see inYrg
     * */
    fun inXrg(x: Double): Boolean {
        val mn = min(startP.x, endP.x)
        val mx = max(startP.x, endP.x)
        return x >= mn && x <= mx
    }

    /**
     * Check if a y value is between the starting point and the ending point of the line, inclusively
     * @param y the y value to check
     * @return true if the y value is between the starting point and the ending point of the line, false otherwise
     * @see inXrg
     * */
    fun inYrg(y: Double): Boolean {
        val mn = min(startP.y, endP.y)
        val mx = max(startP.y, endP.y)
        return y >= mn && y <= mx
    }

    fun isVerti() : Boolean {
        return startP.x errEQ endP.x
    }

    fun isHori() : Boolean {
        return startP.y errEQ endP.y
    }

    fun minXpt() : DPos2 {
        return if (startP.x < endP.x) startP else endP
    }
    fun maxXpt() : DPos2 {
        return if (startP.x > endP.x) startP else endP
    }
    fun minYpt() : DPos2 {
        return if (startP.y < endP.y) startP else endP
    }

    val maxYpt : DPos2
        get(){
        return if (startP.y > endP.y) startP else endP
    }



    override fun plus(shift: DVec2): Line {
        return Line(startP + shift, endP + shift)
    }


    override fun minus(shift: DVec2): Line {
        return Line(startP - shift, endP - shift)
    }

    override fun byPts(_pts: Array<DPos2>): Collidable {
        return Line(_pts[0], _pts[1])
    }

    override fun compareTo(other: Line): Int {
        // first compare the starting point, then ending point
        val startCmp = startP.compareTo(other.startP)
        if (startCmp != 0) return startCmp
        return endP.compareTo(other.endP)
    }

    override fun toShape(coordTransform: (DPos2) -> DPos2, shapeModifier : (Shape) -> Unit): Shape {
        val tStartP = coordTransform(startP)
        val tEndP = coordTransform(endP)
        val ret = Line2D.Double(tStartP.x, tStartP.y, tEndP.x, tEndP.y)
        shapeModifier(ret)
        return ret
    }


}
