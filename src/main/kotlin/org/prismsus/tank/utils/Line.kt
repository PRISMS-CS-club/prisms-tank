package org.prismsus.tank.utils

class Line (startP : Dvec2, endP: Dvec2): Intersectable {
    var slope : Double = (endP.y - startP.y) / (endP.x - startP.x)
    var inter : Double = startP.y - slope * startP.x
    var startP : Dvec2 = startP
        set(new){
            field = new
            slope = (endP.y - startP.y) / (endP.x - startP.x)
            inter = startP.y - slope * startP.x
        }
    var endP : Dvec2 = endP
        set(new){
            field = new
            slope = (endP.y - startP.y) / (endP.x - startP.x)
            inter = startP.y - slope * startP.x
        }

    override fun intersect(other : Intersectable) : Boolean {
        val otherLine = other as Line
        val intersectX = otherLine.inter - inter / (slope - otherLine.slope)
        // calculate the point where intersection happens
        // then check if this point is in the range of both lines
        val inThisRange : Boolean = intersectX >= startP.x && intersectX <= endP.x
        val inOtherRange : Boolean = intersectX >= otherLine.startP.x && intersectX <= otherLine.endP.x
        return inThisRange && inOtherRange
    }

    override fun plus(shift : Dvec2) : Intersectable {
        return Line(startP + shift, endP + shift)
    }

    override fun minus(shift : Dvec2) : Intersectable {
        return plus(-shift)
    }

    override fun rotate(center : Dvec2, rad : Double) : Intersectable {
        var toStartP = startP - center
        var toEndP = endP - center
        toStartP = toStartP.rotate(rad)
        toEndP = toEndP.rotate(rad)
        return Line(toStartP + center, toEndP + center)
    }

}
