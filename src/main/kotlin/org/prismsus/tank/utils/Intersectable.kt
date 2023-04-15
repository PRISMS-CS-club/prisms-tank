package org.prismsus.tank.utils

interface Intersectable {
    /**
     * Check if this object intersects with another object.
     * @param other The other object.
     * @return True if intersects, false otherwise.
     */
    fun intersect(other : Intersectable) : Boolean
    operator fun plus(shift : Dvec2) : Intersectable
    operator fun minus(shift : Dvec2) : Intersectable
    fun rotate(center : Dvec2, rad : Double) : Intersectable
}