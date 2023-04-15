package org.prismsus.tank.utils

class Line (startP : DVec2, endP: DVec2): Intersectable {
    var slope : Double = (endP.y - startP.y) / (endP.x - startP.x)
    var inter : Double = startP.y - slope * startP.x
    var startP : DVec2 = startP
        set(new){
            field = new
            slope = (endP.y - startP.y) / (endP.x - startP.x)
            inter = startP.y - slope * startP.x
        }
    var endP : DVec2 = endP
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
        val inThisRange : Boolean = intersectX > startP.x && intersectX < endP.x
        val inOtherRange : Boolean = intersectX > otherLine.startP.x && intersectX < otherLine.endP.x
        return inThisRange && inOtherRange
    }

    override fun plus(shift : DVec2) : Intersectable {
        return Line(startP + shift, endP + shift)
    }

    override fun minus(shift : DVec2) : Intersectable {
        return plus(-shift)
    }

    override fun rotate(center : DVec2, rad : Double) : Intersectable {
        var toStartP = startP - center
        var toEndP = endP - center
        toStartP = toStartP.rotate(rad)
        toEndP = toEndP.rotate(rad)
        return Line(toStartP + center, toEndP + center)
    }

    override fun rotateAssign(center: Dvec2, rad: Double): Intersectable {
        var toStartP = startP - center
        var toEndP = endP - center
        toStartP = toStartP.rotate(rad)
        toEndP = toEndP.rotate(rad)
        startP = toStartP + center
        endP = toEndP + center
        return this
    }

}
