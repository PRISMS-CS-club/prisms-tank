package org.prismsus.tank.utils

import java.util.*
import kotlin.math.abs
import kotlin.math.atan2

/**
 * Defines a polygonal collision box of a game object.
 * @param pts The points that defines the box. The polygon will be constructed by connecting adjacent points to lines.
 * So that the points should be ordered in a way that the lines will not intersect with each other.
 */
class ColBox(@JvmField var pts : Array<DPos2>): Intersectable{
    // here use the JvmField to restrict the auto generation of getter and setter
    // because we want to override the getter and setter

    /**
     * Construct a box that is in rectangle shape.
     * @param pos The top-left position of the box.
     * @param size The size of the box.
     */
    constructor(pos : DVec2, size : DDim2) : this(arrayOf(
            pos,
            pos + DVec2(size.x, 0.0),
            pos + DVec2(size.x, -size.y),
            pos -DVec2(0.0, size.y)
    )){}

    /**
     * @see Intersectable.rotate
     * */
    override fun rotate(center: DVec2, rad: Double): ColBox {
        var newPts = pts.copyOf()
        for (i in pts.indices){
            var toPt = pts[i] - center
            toPt = toPt.rotate(rad)
            newPts[i] = toPt + center
        }
        return ColBox(newPts)
    }


    /**
     * @see Intersectable.rotateAssign
     * */
    override fun rotateAssign(center: DVec2, rad: Double): ColBox {
        for (i in pts.indices){
            var toPt = pts[i] - center
            toPt = toPt.rotate(rad)
            pts[i] = toPt + center
        }
        return this
    }

    /**
     * @see Intersectable.plus
     * */
    override fun plus(shift: DVec2): ColBox {
        var newPts = pts.copyOf()
        for (i in pts.indices){
            newPts[i] = pts[i] + shift
        }
        return ColBox(newPts)
    }

    /**
     * @see Intersectable.minus
     * */
    override fun minus(shift : DVec2) : ColBox{
        return plus(-shift)
    }


    /**
     * Helper function of [intersect], this function DOES NOT check the situation when one ColBox enclose the other.
     * @param other The other ColBox.
     * @return True if intersects, false otherwise.
     * @see intersect
     * */
    fun intersectNoEnclose(other : Intersectable) : Boolean {
        // does not check the other.intersect(this)
        if (other is DPos2){
            val pt = other as DPos2
            // emit a ray from this point
            // if the ray intersects with the polygon an odd number of times, then the point is inside the polygon
            val sz : Int = pts.size
            val baseRay = Line(pt, pt + DVec2.RT * (Double.MAX_VALUE / 100))
            var interCnt : IntArray = IntArray(DVec2.RTS_DIR.size)
            for (i in 0 until sz){
                val curPolygonLine = Line(pts[i], pts[(i + 1) % sz]) // line formed by current polygon
                for ((i, dir) in DVec2.RTS_DIR.withIndex()){
                    // to prevent touching cases
                    // touching is considered intersection, but touching will give even number of intersections
                    val ray = baseRay + (dir * DOUBLE_PRECISION * 100.0)
                    interCnt[i] += if (curPolygonLine.intersect(ray)) 1 else 0
                }
            }
            return interCnt.any() { it % 2 == 1 }
        }

        for (otherPts : DPos2 in other.getPts()){
            if (intersect(otherPts)) return true
        }
        return false
    }

    /**
    * @see Intersectable.intersect
    * */
    override fun intersect(other : Intersectable) : Boolean {
        if (other !is ColBox) return intersectNoEnclose(other)
        else return intersectNoEnclose(other) || other.intersectNoEnclose(this)
    }

    override fun getPts(): Array<DPos2> {
        return pts
    }
    fun setPts(pts : Array<DPos2>){
        this.pts = pts
    }

    override fun toString(): String {
        return "pts=${pts.contentToString()}"
    }

    /**
     * Check if two ColBox are equal.
     * Notice that two ColBox with different order of points are not considered equal. Since they can form different polygons.
     * To check if two ColBox are having same point set, use [equalPtSet] instead.
     * @param other The other ColBox.
     * @return True if equal, false otherwise.
     * @see equalPtSet
     * */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColBox) return false
        return pts.equals(other.pts)
    }

    /**
     * Check if two intersectable objects are having same point set.
     * Notice that with same point set, the polygon can be different.
     * To check if two ColBox are equal, use [equals] instead.
     * @param other The other ColBox.
     * @return True if equal, false otherwise.
     * @see equals
     * */
    infix fun equalPtSet(other : Intersectable) : Boolean {
        if (other !is ColBox) return false
        val thisSorted = pts.copyOf()
        val otherSorted = other.pts.copyOf()
        thisSorted.sort()
        otherSorted.sort()
        return thisSorted.contentEquals(otherSorted)
    }

    companion object{

        /**
         * Create a ColBox from a set of points that are not ordered
         * @see ColBox (the primary constructor)
         * */
        fun byUnorderedPtSet(pts : Array<DPos2>) : ColBox{
            // sort the points using angle with horizontal line
            val sortedPts = pts.copyOf()
            val avePt : DPos2 = sortedPts.reduce { acc, dPos2 -> acc + dPos2 } / sortedPts.size.toDouble()
            Arrays.sort(sortedPts, 0, sortedPts.size) { o1, o2 ->
                val to1 = o1 - avePt
                val to2 = o2 - avePt
                val ang1 = atan2(to1.y, to1.x)
                val ang2 = atan2(to2.y, to2.x)
                // first sort by angle, then by distance (radius)
                if (abs(ang1 - ang2) > DOUBLE_PRECISION){
                    if (ang1 < ang2) -1 else 1
                } else {
                    if (to1.len() < to2.len()) -1 else 1
                }
            }
            return ColBox(sortedPts)
        }
    }
}