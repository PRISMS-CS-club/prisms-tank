package org.prismsus.tank.utils

/**
 * Defines a polygonal collision box of a game object.
 * @property pts The points of the collision box.
 */
class ColBox(@JvmField var pts : Array<DPos2>): Intersectable {
    // here use the JvmField to restrict the auto generation of getter and setter
    // because we want to override the getter and setter

    /**
     * Construct a box that is in rectangle shape.
     */
    constructor(pos : DVec2, size : DVec2) : this(arrayOf(
            pos,
            pos + DVec2(size.x, 0.0),
            pos + size,
            pos + DVec2(0.0, size.y)
    )){}

    override fun rotate(center: DVec2, rad: Double): ColBox {
        var newPts = pts.copyOf()
        for (i in pts.indices){
            var toPt = pts[i] - center
            toPt = toPt.rotate(rad)
            newPts[i] = toPt + center
        }
        return ColBox(newPts)
    }

    override fun rotateAssign(center: DVec2, rad: Double): ColBox {
        for (i in pts.indices){
            var toPt = pts[i] - center
            toPt = toPt.rotate(rad)
            pts[i] = toPt + center
        }
        return this
    }

    override fun plus(shift: DVec2): ColBox {
        var newPts = pts.copyOf()
        for (i in pts.indices){
            newPts[i] = pts[i] + shift
        }
        return ColBox(newPts)
    }


    override fun minus(shift : DVec2) : ColBox{
        return plus(-shift)
    }

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
}