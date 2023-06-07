package org.prismsus.tank.utils.collidable


import org.prismsus.tank.utils.DOUBLE_PRECISION
import org.prismsus.tank.utils.DVec2
import org.prismsus.tank.utils.treeDistinct
import java.awt.Shape
import java.awt.geom.Path2D
import java.util.*
import kotlin.collections.*
import kotlin.math.*

/**
 * Defines a polygonal collision box of a game object.
 * @param pts The points that defines the box. The polygon will be constructed by connecting adjacent points to lines.
 * So that the points should be ordered in a way that the lines will not intersect with each other.
 */


open class ColPoly(override var pts: Array<DPos2>) : Collidable {
    // here use the JvmField to restrict the auto generation of getter and setter
    // because we want to override the getter and setter
    var rCenter = (pts.reduce { acc, dPos2 -> (acc + dPos2).toPt() }.toVec() / pts.size.toDouble()).toPt()

    // since there will usually be more points in ColPoly, so we don't calculate the center every time
    // instead, we keep the center, and update it when the points are updated
    override var rotationCenter: DPos2
        get() = rCenter
        set(value) {
            val diff = value - rCenter
            pts = pts.map { it + diff }.toTypedArray()
            rCenter = value
        }
    override var angleRotated = 0.0
    override fun plus(shift: DVec2): ColPoly {
            rCenter = rCenter + shift
            return super.plus(shift) as ColPoly
    }

    override fun minus(shift: DVec2): ColPoly {
        rCenter = rCenter - shift
        return super.minus(shift) as ColPoly
    }

    override fun plusAssign(shift: DVec2) {
        rCenter = rCenter + shift
        super.plusAssign(shift)
    }

    override fun minusAssign(shift: DVec2) {
        rCenter = rCenter - shift
        super.minusAssign(shift)
    }


    open val height : Double
        get() = pts.maxBy { it.y }!!.y - pts.minBy { it.y }!!.y
    open val width : Double
        get() = pts.maxBy { it.x }!!.x - pts.minBy { it.x }!!.x
    constructor(vararg xys: Double) : this(xys.mapIndexed { index, d -> if (index % 2 == 0) DPos2(d, xys[index + 1]) else null }.filterNotNull().toTypedArray()) {
    }

    /**
     * Helper function of [collidePts], this function only check the sitution when this box enclose the other box, not the other way around.
     * @param other The other ColPoly.
     * @return True if intersects, false otherwise.
     * @see collidePts
     * */
    override infix fun enclosedPts(other: Collidable): Array<DPos2> {
        // does not check the other.intersect(this)
        if (other is DPos2) {
            val pt = other as DPos2
            // emit a ray from this point
            // if the ray intersects with the polygon an odd number of times, then the point is inside the polygon
            val sz: Int = pts.size
            val ray = Line(pt, pt + DVec2.RT * (Double.MAX_VALUE / 100))
            var interCnt = 0
            for (i in 0 until sz) {
                val curPolygonLine = Line(pts[i], pts[(i + 1) % sz]) // line formed by current polygon

                // to prevent touching cases
                // touching is considered intersection, but touching will give even number of intersections
                val curInterPts = curPolygonLine.collidePts(ray)
                interCnt += if (curInterPts.isNotEmpty()) 1 else 0

            }
            if (interCnt % 2 == 1) return arrayOf(pt)
            return emptyArray()
        }

        val ret = ArrayList<DPos2>()

        for (otherPts: DPos2 in other.pts) {
            ret += enclosedPts(otherPts)
        }
        return ret.toTypedArray()
    }


    override fun intersectPts(other: Collidable): Array<DPos2> {
        val ret = ArrayList<DPos2>()
        if (other is DPos2 || other is Line) {
            for (line in lines) {
                ret.addAll(line intersectPts other)
            }
            return ret.toTypedArray()
        }

        if (other is ColPoly) {
            for (line in lines) {
                for (otherLine in other.lines) {
                    ret.addAll(line intersectPts otherLine)
                }
            }
        }
        return ret.toTypedArray()
    }

    /**
     * Construct a graph of the contour of the polygon, when intersect with another polygon
     * @return A pair containing the graph and a boolean array indicating if the point is belong (point on this polygon's line) to this polygon.
     * */
    fun contourGraph(
        other: ColPoly,
        collideResult: Array<DPos2>
    ): Pair<TreeMap<DPos2, ArrayList<DPos2>>, TreeMap<DPos2, Boolean>> {
        val edge = TreeMap<DPos2, ArrayList<DPos2>>()
        val isThisPt = TreeMap<DPos2, Boolean>()
        val addEdge = { pt1: DPos2, pt2: DPos2 ->
            if (pt1 != pt2){
                if (edge[pt1] == null) edge[pt1] = ArrayList()
                edge[pt1]!!.add(pt2)
                if (edge[pt2] == null) edge[pt2] = ArrayList()
                edge[pt2]!!.add(pt1)
            }
        }

        val ptsOnLines = TreeMap<Line, ArrayList<DPos2>>()

        for (line in lines) {
            ptsOnLines[line] = ArrayList()
            ptsOnLines[line]!!.add(line.startP)
            ptsOnLines[line]!!.add(line.endP)
            for (interPt in collideResult) {
                if (line intersect interPt) {
                    ptsOnLines[line]!!.add(interPt)
                }
            }
        }

        for (line in other.lines) {
            ptsOnLines[line] = ArrayList()
            ptsOnLines[line]!!.add(line.startP)
            ptsOnLines[line]!!.add(line.endP)
            for (interPt in collideResult) {
                if (line intersect interPt) {
                    ptsOnLines[line]!!.add(interPt)
                }
            }
        }

        // sort the points according to their distance to the starting point to the line
        for (line in ptsOnLines.keys) {
            ptsOnLines[line] = ptsOnLines[line]!!.treeDistinct() as ArrayList<DPos2>
            ptsOnLines[line]!!.sortWith(Comparator { o1, o2 ->
                val d1 = (o1 - line.startP).sqLen()
                val d2 = (o2 - line.startP).sqLen()
                if (d1 < d2) -1 else if (d1 > d2) 1 else 0
            })
        }

        // connect the points on the same line

        for (line in ptsOnLines.keys) {
            val interPts = ptsOnLines[line]!!
            for (i in 0 until interPts.size - 1) {
                addEdge(interPts[i], interPts[i + 1])
            }
        }
        return Pair(edge, isThisPt)
    }

    infix fun contourGraph(other: ColPoly): Pair<TreeMap<DPos2, ArrayList<DPos2>>, TreeMap<DPos2, Boolean>> {
        return contourGraph(other, intersectPts(other))
    }

    var lines: Array<Line>
        get() {
            // connect adjacent points to lines
            return Array(pts.size) { i -> Line(pts[i], pts[(i + 1) % pts.size]) }
        }
        set(value) {}

    /**
     * @see Collidable.collidePts
     * */
    override fun collidePts(other: Collidable): Array<DPos2> {
        if (other !is ColPoly) return (enclosedPts(other) + intersectPts(other))
        val thisRet = enclosedPts(other)
        val otherRet = other.enclosedPts(this)
        // delete repeated points
        return (thisRet + otherRet + intersectPts(other)).treeDistinct().toTypedArray()
    }

    override fun toString(): String {
        return "pts=${pts.contentToString()}"
    }

    /**
     * Check if two ColPoly are equal.
     * Notice that two ColPoly with different order of points are not considered equal. Since they can form different polygons.
     * They are only considered equal if every adjacent points are the same, in a circular way.
     * Meaning that the polygon still have the same set of lines.
     * To check if two ColPoly are having same point set, use [equalPtSet] instead.
     * @param other The other ColPoly.
     * @return True if equal, false otherwise.
     * @see equalPtSet
     * */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColPoly) return false
        val otherStartIdx = other.pts.indexOf(pts[0])
        if (otherStartIdx == -1) return false
        for (i in pts.indices) {
            if (pts[i] != other.pts[(otherStartIdx + i) % pts.size]) return false
        }
        return true
    }

    /**
     * Check if two intersectable objects are having same point set.
     * Notice that with same point set, the polygon can be different.
     * To check if two ColPoly are equal, use [equals] instead.
     * @param other The other ColPoly.
     * @return True if equal, false otherwise.
     * @see equals
     * */
    infix fun equalPtSet(other: Collidable): Boolean {
        if (other !is ColPoly) return false
        val thisSorted = pts.copyOf()
        val otherSorted = other.pts.copyOf()
        thisSorted.sort()
        otherSorted.sort()
        return thisSorted.contentEquals(otherSorted)
    }

    override fun byPts(pts: Array<DPos2>): Collidable {
        return ColPoly(pts)
    }

    override fun rotateAssign(radOffset: Double, center: DPos2): Collidable {
        rCenter = rCenter.toVec().rotate(radOffset).toPt()
        return super.rotateAssign(radOffset, center)
    }

    override fun toShape(coordTransform: (DPos2) -> DPos2, shapeModifier: (Shape) -> Unit): Shape {
        val transformed = pts.map { coordTransform(it) }
        val ret = Path2D.Double(Path2D.WIND_EVEN_ODD)
        ret.moveTo(transformed[0].x, transformed[0].y)
        for (i in 1 until transformed.size) {
            ret.lineTo(transformed[i].x, transformed[i].y)
        }
        ret.closePath()
        shapeModifier(ret)
        return ret
    }

    /**
     * @return the combination of two ColPoly, which is the union of the two polygons.
     * If they do not intersect, return null.
     * Note that if the actual union forms a shape with empty space, the empty space will not be included.
     * */
    infix fun union(other: ColPoly): ColPoly? {
        if (!collide(other)) return null
        if (enclose(other)) return ColPoly(pts.copyOf().map { it.copy() }
            .toTypedArray())
        if (other.enclose(this)) return ColPoly(other.pts.copyOf().map { it.copy() }
            .toTypedArray())

        val (graph, _) = contourGraph(other)
        // for each point, travel to the next point where minimum angle is formed
        // start by the point with minimum x, then y
        var curPt = arrayOf(pts.min(), other.pts.min()).min()
        val vised = TreeSet<DPos2>()
        val ret = ArrayList<DPos2>()
        lateinit var lastVec: DVec2
        var lastPt = (curPt - DPos2(1.0, 0.0)).toPt()
        while (true) {
            lastVec = curPt.toVec() - lastPt.toVec()
            if (vised.contains(curPt)) continue
            vised.add(curPt)
            ret.add(curPt)
            val unvised = graph[curPt]!!.filter { !vised.contains(it) }
            // when at the right side of the last vector, try to make the angle smaller
            var nextPt = unvised.filter { lastVec.isPtAtRight((it - curPt).toPt()) }.minByOrNull {
                lastVec.norm() dot (it - curPt).norm()
            }
            if (nextPt == null)
                nextPt = unvised.maxByOrNull { lastVec.norm() dot (it - curPt).norm() }
            // when at the left side of the last vector, try to make the angle larger
            lastPt = curPt
            if (nextPt == null)
                break
            else
                curPt = nextPt
        }
        return ColPoly(ret.toTypedArray())
    }


    companion object {

        /**
         * Create a ColPoly from a set of points that are not ordered
         * @see ColPoly (the primary constructor)
         * */
        fun byUnorderedPtSet(pts: Array<DPos2>): ColPoly {
            // sort the points using angle with horizontal line
            val sortedPts = pts.copyOf()
            val avePt: DPos2 =
                (sortedPts.reduce { acc, dPos2 -> (acc + dPos2).toPt() }.toVec() / sortedPts.size.toDouble()).toPt()
            Arrays.sort(sortedPts, 0, sortedPts.size) { o1, o2 ->
                val to1 = o1 - avePt
                val to2 = o2 - avePt
                val ang1 = atan2(to1.y, to1.x)
                val ang2 = atan2(to2.y, to2.x)
                // first sort by angle, then by distance (radius)
                if (abs(ang1 - ang2) > DOUBLE_PRECISION) {
                    if (ang1 < ang2) -1 else 1
                } else {
                    if (to1.len() < to2.len()) -1 else 1
                }
            }
            return ColPoly(sortedPts)
        }


    }
}