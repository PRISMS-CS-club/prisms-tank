package org.prismsus.tank.utils

interface Intersectable {


    /**
     * This is intended to be a static method, but Kotlin does not support static methods in interfaces.
     * Create an intersectable object from a list of points.
     *  */
    fun byPts(pts: Array<DPos2>): Intersectable
    /**
     * Check if this object intersects with another object.
     * @param other The other object.
     * @return True if intersects, false otherwise.
     */
    infix fun intersect(other : Intersectable) : Boolean
    /**
     * shifting the intersectable object by a vector
     * @param shift The vector to shift.
     * @return The shifted object.
     * */
    operator fun plus(shift : DVec2) : Intersectable {
        return byPts(pts.map{it + shift}.toTypedArray())
    }

    operator fun plusAssign(shift : DVec2) {
        pts = pts.map{it + shift}.toTypedArray()
    }

    /**
     * shifting the intersectable object by a vector in the reverse direction, equivlant to plus(-shift)
     * @param shift The vector to shift.
     * @return The shifted object.
     * @see plus
     * */
    operator fun minus(shift : DVec2) : Intersectable {
        return byPts(pts.map{it - shift}.toTypedArray())
    }

    operator fun minusAssign(shift : DVec2) {
        pts = pts.map{it - shift}.toTypedArray()
    }

    /**
     * rotate the intersectable object by a radian
     * @param center The center, or pivot of rotation.
     * @param rad The radian to rotate.
     * @return The rotated object.
     * */
    fun rotate(rad: Double, center: DPos2 = rotationCenter) : Intersectable {
        var newPts = pts.copyOf().map { it.copy()}.toTypedArray()
        return byPts(newPts).rotateAssign(rad, center)
    }

    /**
     * rotate the intersectable object by a degree
     * @param center The center, or pivot of rotation.
     * @param degOffset The degree to rotate.
     * @return The rotated object.
     * @see rotate
    * */
    fun rotateDeg(degOffset: Double, center: DPos2 = rotationCenter) : Intersectable {
        return rotate(degOffset.toRad(), center)
    }
    /*
     * rotate this intersectable object by a radian, and assign the result to this object
     * @param center The center, or pivot of rotation.
     * @param rad The radian to rotate.
     * @return The rotated object.
     * @see rotate
    * */
    fun rotateAssign(radOffset: Double, center: DPos2 = rotationCenter) : Intersectable {
        pts = pts.map{it.rotateAssign(radOffset, center) as DPos2}.toTypedArray()
        return this
    }

    /**
     * rotate this intersectable object by a degree, and assign the result to this object
     * @param center The center, or pivot of rotation.
     * @param degOffset The degree to rotate.
     * @return The rotated object.
     * @see rotate
    * */
    fun rotateAssignDeg(degOffset: Double, center: DPos2 = rotationCenter) : Intersectable {
        return rotateAssign(degOffset / 180.0 * Math.PI, center)
    }

    /**
     * Given a center, calculate the angle rotated around this center compared to the
     * original intersectable object.
     * */
    fun angleRotated(center : DPos2) : Double{
        val curAngVec = (rotationCenter - center)
        val origAngVec = (unrotated.rotationCenter - center)
        val vecAng = curAngVec.angleWith(origAngVec)
        // this is the smaller angle between two vectors
        // need to check which side is curAngVec to origAngVec
        var ret = 0.0
        if (origAngVec.isPtAtLeft(curAngVec.toPt())){
            ret = vecAng
        } else {
            ret = -vecAng
        }
        return ret
    }

    /*
    * The following variables are used to indicate the position and size of the
    * image for the object. Since images are always rectangles, we can use these
    * */
    var angleRotated : Double     // the angle offset from the original position (rotate by center), in radian
        get() {return angleRotated(rotationCenter)}
        set(x){

        }
    var encSquareSize : DDim2     // the size of the enclosing square, which is the size of the image
        get() {
            // calculate the size of the image using reduce
            val min = pts.reduce { acc, dPos2 -> acc.min(dPos2) }
            val max = pts.reduce { acc, dPos2 -> acc.max(dPos2) }
            return (max - min)
        }
        set(x){
            throw Exception("Cannot set size of the image")
        }

    val unrotated : Intersectable // the unrotated version of the object, used for a more accurate rotation
    var rotationCenter : DPos2    // the center of the object, used for rotation
        get() {
            // calculate the average of the points using reduce
            return (pts.reduce { acc, dPos2 -> (acc + dPos2).toPt()}.toVec() / pts.size.toDouble()).toPt()
        }
        set(x){
            throw Exception("Cannot set rotation center")
        }
    var pts : Array<DPos2>        // the points of the object, used for intersection
}