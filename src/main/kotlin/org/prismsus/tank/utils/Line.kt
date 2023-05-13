package org.prismsus.tank.utils

class Line (startP : DPos2, endP: DPos2): Intersectable {
    var slope : Double = (endP.y - startP.y) / (endP.x - startP.x)
    var inter : Double = startP.y - slope * startP.x
    var startP : DPos2 = startP
        set(new){
            field = new
            slope = (endP.y - startP.y) / (endP.x - startP.x)
            inter = startP.y - slope * startP.x
        }
    var endP : DPos2 = endP
        set(new){
            field = new
            slope = (endP.y - startP.y) / (endP.x - startP.x)
            inter = startP.y - slope * startP.x
        }

    init{
        assert(startP.x <= endP.x, {"start point must be on the left of end point"})
    }

    override fun intersect(other : Intersectable) : Boolean {
        val otherLine = other as Line
        // check if two lines are parallel, in this case, they will never intersect
        if (slope == otherLine.slope) {
            if (inter != otherLine.inter) {
                return false
            }
            // slope and intercept are all the same
            // now check if they overlap within the range
            val inThisRange : Boolean = otherLine.startP.x >= startP.x && otherLine.startP.x <= endP.x
                    || otherLine.endP.x >= startP.x && otherLine.endP.x <= endP.x
            // either starting point or ending point in the range of this line
            val inOtherRange : Boolean = startP.x >= otherLine.startP.x && startP.x <= otherLine.endP.x
                    || endP.x >= otherLine.startP.x && endP.x <= otherLine.endP.x
            return inThisRange || inOtherRange
            // using or here considering the case where one line is within the other line
        }
        val intersectX = (otherLine.inter - inter) / (slope - otherLine.slope)
        // calculate the point where intersection happens
        // then check if this point is in the range of both lines
        val inThisRange : Boolean = intersectX >= startP.x && intersectX <= endP.x
        val inOtherRange : Boolean = intersectX >= otherLine.startP.x && intersectX <= otherLine.endP.x
        return inThisRange && inOtherRange
    }

    override fun plus(shift : DVec2) : Intersectable {
        return Line(startP + shift, endP + shift)
    }

    override fun minus(shift : DVec2) : Intersectable {
        return plus(-shift)
    }

    override fun rotate(center : DPos2, rad : Double) : Intersectable {
        var toStartP = startP - center
        var toEndP = endP - center
        toStartP = toStartP.rotate(rad)
        toEndP = toEndP.rotate(rad)
        return Line(toStartP + center, toEndP + center)
    }

    override fun rotateAssign(center: DPos2, rad: Double): Intersectable {
        var toStartP = startP - center
        var toEndP = endP - center
        toStartP = toStartP.rotate(rad)
        toEndP = toEndP.rotate(rad)
        startP = toStartP + center
        endP = toEndP + center
        return this
    }

    override fun equals(other : Any?) : Boolean{
        if (other !is Line) {
            return false
        }
        return startP == other.startP && endP == other.endP
    }

    override fun toString() : String {
        return "$startP -> $endP, slope: $slope, intercept: $inter"
    }
}
