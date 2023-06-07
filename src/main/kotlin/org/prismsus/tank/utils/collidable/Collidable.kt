package org.prismsus.tank.utils.collidable
import org.prismsus.tank.utils.*
import java.awt.Shape
import kotlin.math.*

interface Collidable {


    /**
     * This is intended to be a static method, but Kotlin does not support static methods in interfaces.
     * Create an collidable object from a list of points.
     *  */
    fun byPts(pts: Array<DPos2>): Collidable
    /**
     * Check if this object collides with another object.
     * Intersections and enclosure will all be considered as collision
     * @param other The other object.
     * @return True if collides, false otherwise.
     */
    infix fun collide(other : Collidable) : Boolean {
        return enclosedPts(other).isNotEmpty() || intersect(other)
    }

    infix fun collidePts(other : Collidable) : Array<DPos2> {
        return intersectPts(other) + enclosedPts(other)
    }

    infix fun intersectPts(other : Collidable) : Array<DPos2>

    infix fun enclosedPts(other : Collidable) : Array<DPos2>

    infix fun enclose (other : Collidable) : Boolean {
        return enclosedPts(other).size == other.pts.size
    }

    infix fun intersect(other : Collidable) : Boolean {
        return intersectPts(other).isNotEmpty()
    }

    /**
     * shifting the collidable object by a vector
     * @param shift The vector to shift.
     * @return The shifted object.
     * */
    operator fun plus(shift : DVec2) : Collidable {
        for (pt in pts){
            pt += shift
        }
        return this
    }

    infix fun shift(shift : DVec2) : Collidable {
        return plus(shift)
    }

    infix fun shiftAssign(shift : DVec2) {
        plusAssign(shift)
    }

    infix fun shiftTo(pt : DPos2) : Collidable {
        val shift = pt - pts[0]
        return plus(shift)
    }

    infix fun shiftToAssign(pt : DPos2) {
        val shift = pt - pts[0]
        plusAssign(shift)
    }

    operator fun plusAssign(shift : DVec2) {
        for (pt in pts){
            pt += shift
        }
    }

    /**
     * shifting the collidable object by a vector in the reverse direction, equivlant to plus(-shift)
     * @param shift The vector to shift.
     * @return The shifted object.
     * @see plus
     * */
    operator fun minus(shift : DVec2) : Collidable {
        return byPts(pts.map{it - shift}.toTypedArray())
    }

    operator fun minusAssign(shift : DVec2) {
        for (pt in pts){
            pt -= shift
        }
    }

    /**
     * rotate the collidable object by a radian
     * @param center The center, or pivot of rotation.
     * @param rad The radian to rotate.
     * @return The rotated object.
     * */

    fun rotate(rad: Double, center: DPos2 = rotationCenter) : Collidable {
        val newPts = pts.copyOf().map { it.copy()}.toTypedArray()
        angleRotated += rad
        return byPts(newPts).rotateAssign(rad, center)
    }

    fun rotateTo(rad: Double, center: DPos2 = rotationCenter) : Collidable {
        val rotAng = rad - angleRotated
        return rotate(rotAng, center)
    }

    /**
     * rotate the collidable object by a degree
     * @param center The center, or pivot of rotation.
     * @param degOffset The degree to rotate.
     * @return The rotated object.
     * @see rotate
    * */
    fun rotateDeg(degOffset: Double, center: DPos2 = rotationCenter) : Collidable {
        return rotate(degOffset.toRad(), center)
    }
    fun rotateToDeg(degOffset: Double, center: DPos2 = rotationCenter) : Collidable {
        val rotAng = degOffset.toRad() - angleRotated
        return rotate(rotAng, center)
    }

    /*
     * rotate this collidable object by a radian, and assign the result to this object
     * @param center The center, or pivot of rotation.
     * @param rad The radian to rotate.
     * @return The rotated object.
     * @see rotate
    * */
    fun rotateAssign(radOffset: Double, center: DPos2 = rotationCenter) : Collidable {
        angleRotated += radOffset
        for (pt in pts){
            pt.rotateAssign(radOffset, center)
        }
        return this
    }

    fun rotateAssignTo(radOffset: Double, center: DPos2 = rotationCenter) : Collidable {
        val rotAng = radOffset - angleRotated
        return rotateAssign(rotAng, center)
    }

    /**
     * rotate this collidable object by a degree, and assign the result to this object
     * @param center The center, or pivot of rotation.
     * @param degOffset The degree to rotate.
     * @return The rotated object.
     * @see rotate
    * */
    fun rotateAssignDeg(degOffset: Double, center: DPos2 = rotationCenter) : Collidable {
        return rotateAssign(degOffset / 180.0 * Math.PI, center)
    }
    fun rotateAssignToDeg(degOffset: Double, center: DPos2 = rotationCenter) : Collidable {
        val rotAng = degOffset / 180.0 * Math.PI - angleRotated
        return rotateAssign(rotAng, center)
    }


    fun copy() : Collidable {
        val newPts = pts.copyOf().map { it.copy()}.toTypedArray()
        return byPts(newPts)
    }

    fun toShape(coordTransform : (DPos2) -> DPos2 = {it}, shapeModifier : (Shape) -> Unit = {it}) : Shape


    /*
    * The following variables are used to indicate the position and size of the
    * image for the object. Since images are always rectangles, we can use these
    * */
    var angleRotated : Double     // the angle offset from the original position (rotate by center), in radians
    val encAARectSize : DDim2     // the size of the enclosing square, which is the size of the image
        get() {
            // calculate the size of the image using reduce
            val x = maxX - minX
            val y = maxY - minY
            return DDim2(x, y)
        }

    val minX : Double
        get(){
        var ret = Double.MAX_VALUE
        for (pt in pts){
            ret = min(ret, pt.x)
        }
        return ret
    }

    val maxX : Double
        get(){

            var ret = -Double.MAX_VALUE
            for (pt in pts){
                ret = max(ret, pt.x)
            }
            return ret
        }

    val minY : Double
        get(){
            var ret = Double.MAX_VALUE
            for (pt in pts){
                ret = min(ret, pt.y)
            }
            return ret
        }

    val maxY : Double
        get(){
            var ret = -Double.MAX_VALUE
            for (pt in pts){
                ret = max(ret, pt.y)
            }
            return ret
        }

    val encAARect : ColAArect   // the enclosing rectangle, which is the image
        get() {
            val dim = encAARectSize
            val bottomLeft = DPos2(minX, minY)
            return ColAArect.byBottomLeft(bottomLeft, dim)
        }


    val unrotated : Collidable // the unrotated version of the object, used for a more accurate rotation
        get() {
            return rotate(-angleRotated)
        }
    var rotationCenter : DPos2    // the center of the object, used for rotation
        get() {
            val ave = DVec2()
            for (pt in pts){
                ave += pt.toVec()
            }
            return (ave / pts.size.toDouble()).toPt()
        }
        set(x){
            val vec = x - rotationCenter
            for (pt in pts){
                pt += vec
            }
        }
    var pts : Array<DPos2>        // the points of the object, used for intersection
}