package org.prismsus.tank.utils

interface Intersectable {
    /**
     * Check if this object intersects with another object.
     * @param other The other object.
     * @return True if intersects, false otherwise.
     */
    fun intersect(other : Intersectable) : Boolean
    /**
     * shifting the intersectable object by a vector
     * @param shift The vector to shift.
     * @return The shifted object.
     * */
    operator fun plus(shift : DVec2) : Intersectable

    /**
     * shifting the intersectable object by a vector in the reverse direction, equivlant to plus(-shift)
     * @param shift The vector to shift.
     * @return The shifted object.
     * @see plus
     * */
    operator fun minus(shift : DVec2) : Intersectable

    /**
     * rotate the intersectable object by a radian
     * @param center The center, or pivot of rotation.
     * @param rad The radian to rotate.
     * @return The rotated object.
     * */
    fun rotate(center : DPos2, rad : Double) : Intersectable

    /**
     * rotate the intersectable object by a degree
     * @param center The center, or pivot of rotation.
     * @param deg The degree to rotate.
     * @return The rotated object.
     * @see rotate
    * */
    fun rotateDeg(center : DPos2, deg : Double) : Intersectable {
        return rotate(center, deg / 180.0 * Math.PI)
    }
    /*
     * rotate this intersectable object by a radian, and assign the result to this object
     * @param center The center, or pivot of rotation.
     * @param rad The radian to rotate.
     * @return The rotated object.
     * @see rotate
    * */
    fun rotateAssign(center : DVec2, rad : Double) : Intersectable

    /**
     * rotate this intersectable object by a degree, and assign the result to this object
     * @param center The center, or pivot of rotation.
     * @param deg The degree to rotate.
     * @return The rotated object.
     * @see rotate
    * */
    fun rotateAssignDeg(center : DVec2, deg : Double) : Intersectable {
        return rotateAssign(center, deg / 180.0 * Math.PI)
    }
    fun getPts() : Array<DPos2>
}