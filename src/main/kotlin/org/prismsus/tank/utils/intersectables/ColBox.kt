package org.prismsus.tank.utils.intersectables


import org.prismsus.tank.utils.DOUBLE_PRECISION
import org.prismsus.tank.utils.DVec2
import java.awt.Color
import java.util.*
import javax.swing.JPanel
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.atan2

/**
 * Defines a polygonal collision box of a game object.
 * @param pts The points that defines the box. The polygon will be constructed by connecting adjacent points to lines.
 * So that the points should be ordered in a way that the lines will not intersect with each other.
 */


open class ColBox(override var pts : Array<DPos2>): Intersectable {
    // here use the JvmField to restrict the auto generation of getter and setter
    // because we want to override the getter and setter
    val origPts = pts.copyOf()
    var rCenter = (pts.reduce { acc, dPos2 -> (acc + dPos2).toPt()}.toVec() / pts.size.toDouble()).toPt()
    // since there will usually be more points in ColBox, so we don't calculate the center every time
    // instead, we keep the center, and update it when the points are updated
    override var rotationCenter: DPos2
        get() = rCenter
        set(value) {}
    override val unrotated: Intersectable
        get() = ColBox(origPts)
    override var angleRotated = 0.0


    /**
     * Helper function of [intersectPts], this function only check the sitution when this box enclose the other box, not the other way around.
     * @param other The other ColBox.
     * @return True if intersects, false otherwise.
     * @see intersectPts
     * */
    fun intersectPtsEncloseOther(other : Intersectable) : Array<DPos2> {
        // does not check the other.intersect(this)
        if (other is DPos2){
            val pt = other as DPos2
            // emit a ray from this point
            // if the ray intersects with the polygon an odd number of times, then the point is inside the polygon
            val sz : Int = pts.size
            val baseRay = Line(pt, pt + DVec2.RT * (Double.MAX_VALUE / 100))
            val offsetsInterCnt : IntArray = IntArray(DVec2.RTS_DIR.size) // intersection count for every base ray offsets
            val offsetsInterPts : Array<ArrayList<DPos2>> = Array(DVec2.RTS_DIR.size) { ArrayList<DPos2>() } // intersection points for every base ray offsets
            for (i in 0 until sz){
                val curPolygonLine = Line(pts[i], pts[(i + 1) % sz]) // line formed by current polygon
                for ((i, dir) in DVec2.RTS_DIR.withIndex()){
                    // to prevent touching cases
                    // touching is considered intersection, but touching will give even number of intersections
                    val ray = baseRay + (dir * DOUBLE_PRECISION * 100.0)
                    val curInterPts = curPolygonLine.intersectPts(ray)
                    offsetsInterCnt[i] += if (curInterPts.isNotEmpty()) 1 else 0
                    offsetsInterPts[i] += curInterPts
                }
            }
            val ret = offsetsInterPts.filterIndexed { i, _ -> offsetsInterCnt[i] % 2 == 1 }
            if (ret.isEmpty()) return arrayOf()
            return ret.first().toTypedArray()
        }

        val ret = ArrayList<DPos2>()
        for (otherPts : DPos2 in other.pts){
            ret += intersectPtsEncloseOther(otherPts)
        }
        return ret.toTypedArray()
    }

    /**
    * @see Intersectable.intersectPts
    * */
    override fun intersectPts(other : Intersectable) : Array<DPos2> {
        if (other !is ColBox) return intersectPtsEncloseOther(other)
        val thisRet = intersectPtsEncloseOther(other)
        val otherRet = other.intersectPtsEncloseOther(this)
        return thisRet + otherRet
    }

    override fun toString(): String {
        return "pts=${pts.contentToString()}"
    }

    /**
     * Check if two ColBox are equal.
     * Notice that two ColBox with different order of points are not considered equal. Since they can form different polygons.
     * They are only considered equal if every adjacent points are the same, in a circular way.
     * Meaning that the polygon still have the same set of lines.
     * To check if two ColBox are having same point set, use [equalPtSet] instead.
     * @param other The other ColBox.
     * @return True if equal, false otherwise.
     * @see equalPtSet
     * */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColBox) return false
        val otherStartIdx = other.pts.indexOf(pts[0])
        if (otherStartIdx == -1) return false
        for (i in pts.indices){
            if (pts[i] != other.pts[(otherStartIdx + i) % pts.size]) return false
        }
        return true
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

    override fun byPts(pts: Array<DPos2>): Intersectable {
        return ColBox(pts)
    }

    override fun rotateAssign(radOffset: Double, center: DPos2): Intersectable {
        rCenter = rCenter.toVec().rotate(radOffset).toPt()
        return super.rotateAssign(radOffset, center)
    }

    override fun drawGraphics(panel: JPanel, factor: Double) {
        val g = panel.graphics
        val screenPts = ptsAsScreenIdx(panel.height, factor)
        g.color = Color.BLACK
        val xArr = screenPts.map{it.x.toInt()}.toIntArray()
        val yArr = screenPts.map{it.y.toInt()}.toIntArray()
        g.drawPolygon(xArr, yArr, pts.size)
    }

    companion object{

        /**
         * Create a ColBox from a set of points that are not ordered
         * @see ColBox (the primary constructor)
         * */
        fun byUnorderedPtSet(pts : Array<DPos2>) : ColBox {
            // sort the points using angle with horizontal line
            val sortedPts = pts.copyOf()
            val avePt : DPos2 = (sortedPts.reduce { acc, dPos2 -> (acc + dPos2).toPt()}.toVec() / sortedPts.size.toDouble()).toPt()
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