package org.prismsus.tank.utils

class ColBox(var pts : Array<Dvec2>): Intersectable {
    lateinit var lines : Array<Line>
    init{
        if(pts.size != 4){
            throw IllegalArgumentException("ColBox must have 4 points")
        }
        lines = Array(4, {i -> Line(pts[i], pts[(i + 1) % 4])})
    }

    override fun rotate(center: Dvec2, rad: Double): Intersectable {
        var newPts = pts.copyOf()
        for (i in pts.indices){
            var toPt = pts[i] - center
            toPt = toPt.rotate(rad)
            newPts[i] = toPt + center
        }
        return ColBox(newPts)
    }

    override fun plus(shift: Dvec2): Intersectable {
        var newPts = pts.copyOf()
        for (i in pts.indices){
            newPts[i] = pts[i] + shift
        }
        return ColBox(newPts)
    }

    override fun minus(shift : Dvec2) : Intersectable{
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