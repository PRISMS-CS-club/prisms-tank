package org.prismsus.tank.utils

/**
 * Defines a polygonal collision box of a game object.
 * @property pts The points of the collision box.
 */
class ColBox(var pts : Array<DVec2>): Intersectable {
    var lines : Array<Line>
    init{
        if(pts.size != 4){
            throw IllegalArgumentException("ColBox must have 4 points")
        }
        lines = Array(4, {i -> Line(pts[i], pts[(i + 1) % 4])})
    }

    /**
     * Construct a box that is in rectangle shape.
     */
    constructor(pos : DVec2, size : DVec2) : this(arrayOf(
            pos,
            pos + DVec2(size.x, 0.0),
            pos + size,
            pos + DVec2(0.0, size.y)
    )){}

    override fun rotate(center: DVec2, rad: Double): Intersectable {
        var newPts = pts.copyOf()
        for (i in pts.indices){
            var toPt = pts[i] - center
            toPt = toPt.rotate(rad)
            newPts[i] = toPt + center
        }
        return ColBox(newPts)
    }

    override fun rotateAssign(center: DVec2, rad: Double): Intersectable {
        for (i in pts.indices){
            var toPt = pts[i] - center
            toPt = toPt.rotate(rad)
            pts[i] = toPt + center
        }
        return this
    }

    override fun plus(shift: DVec2): Intersectable {
        var newPts = pts.copyOf()
        for (i in pts.indices){
            newPts[i] = pts[i] + shift
        }
        return ColBox(newPts)
    }


    override fun minus(shift : DVec2) : Intersectable{
        return plus(-shift)
    }

    override fun intersect(other : Intersectable) : Boolean {

        for (thisLine in lines){
            if (other is ColBox)
            for (otherLine in other.lines){
                if(thisLine.intersect(otherLine)){
                    return true
                }
            }
            else if (other is Line){
                if(thisLine.intersect(other)){
                    return true
                }
            }
        }
        return false
    }
}