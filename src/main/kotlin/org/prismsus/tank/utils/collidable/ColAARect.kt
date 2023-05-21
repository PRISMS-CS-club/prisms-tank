package org.prismsus.tank.utils.collidable

import org.prismsus.tank.utils.*

/*
* Axis Aligned Rectangle Collision Box, cannot rotate, can make collision detection faster
* */
class ColAARect(centerPos : DPos2, size : DDim2) : ColRect(centerPos, size){

    companion object{
        fun byTopLeft(topLeft : DPos2, size : DDim2) : ColAARect {
            val cent = topLeft + DVec2(size.x / 2, -size.y / 2)
            return ColAARect(cent, size)
        }
    }

    override fun rotateAssign(radOffset: Double, center: DPos2): Collidable {
        throw UnsupportedOperationException("ColAARect cannot rotate")
    }

    override fun rotateAssignDeg(degOffset: Double, center: DPos2): Collidable {
        throw UnsupportedOperationException("ColAARect cannot rotate")
    }

    override fun rotateAssignTo(radOffset: Double, center: DPos2): Collidable {
        throw UnsupportedOperationException("ColAARect cannot rotate")
    }

    override fun rotateAssignToDeg(degOffset: Double, center: DPos2): Collidable {
        throw UnsupportedOperationException("ColAARect cannot rotate")
    }

    override fun collide(other: Collidable): Boolean {
        for (pt in other.pts){
            if (pt.x <= maxX && pt.x >= minX && pt.y <= maxY && pt.y >= minY){
                return true
            }
        }
        return false
    }

    override fun collidePts(other: Collidable): Array<DPos2> {
        val ret = ArrayList<DPos2>()
        for (pt in other.pts){
            if (pt.x <=  maxX && pt.x >= minX && pt.y <= maxY && pt.y >= minY){
                ret.add(pt)
            }
        }
        return ret.toTypedArray()
    }

    override fun enclosedPts(other: Collidable): Array<DPos2> {
        val ret = ArrayList<DPos2>()
        for (pt in other.pts){
            if (pt.x < maxX && pt.x > minX && pt.y < maxY && pt.y > minY){
                ret.add(pt)
            }
        }
        return ret.toTypedArray()
    }

    override fun enclose(other: Collidable): Boolean {
        if (other is ColAARect){
            return maxX > other.maxX && minX < other.minX && maxY > other.maxY && minY < other.minY
        }
        for (pt in other.pts){
            if (pt.x < maxX && pt.x > minX && pt.y < maxY && pt.y > minY){
                return true
            }
        }
        return false
    }

    override fun intersectPts(other: Collidable): Array<DPos2> {
        val ret = ArrayList<DPos2>()
        for (pt in other.pts){
            if (pt.x > maxX || pt.x < minX || pt.y > maxY || pt.y < minY){
                continue;
            }
            if (pt.x errEQ maxX || pt.x errEQ minX || pt.y errEQ maxY || pt.y errEQ minY){
                ret.add(pt)
            }
        }
        return ret.toTypedArray()
    }

    override fun intersect(other: Collidable): Boolean {
        for (pt in other.pts){
            if (pt.x > maxX || pt.x < minX || pt.y > maxY || pt.y < minY){
                continue;
            }
            if (pt.x errEQ maxX || pt.x errEQ minX || pt.y errEQ maxY || pt.y errEQ minY){
                return true;
            }
        }
        return false;
    }

    override var angleRotated: Double
        get() = 0.0
        set(value) {}

    override val encAARect: ColAARect
        get() = this
}