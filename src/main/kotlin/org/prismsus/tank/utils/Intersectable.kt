package org.prismsus.tank.utils

interface Intersectable {
    /**
     * Check if this object intersects with another object.
     * @param other The other object.
     * @return True if intersects, false otherwise.
     */

    /**
        * Check if this object intersects with another object.
        * Even if the objects are touching, they are not considered intersecting.
     */
    fun intersect(other : Intersectable) : Boolean

    operator fun plus(shift : DVec2) : Intersectable
    operator fun minus(shift : DVec2) : Intersectable
    fun rotate(center : DPos2, rad : Double) : Intersectable
    fun rotateDeg(center : DPos2, deg : Double) : Intersectable {
        return rotate(center, deg / 180.0 * Math.PI)
    }
    fun rotateAssign(center : DVec2, rad : Double) : Intersectable
    fun rotateAssignDeg(center : DVec2, deg : Double) : Intersectable {
        return rotateAssign(center, deg / 180.0 * Math.PI)
    }
    fun getPts() : Array<DPos2>
}