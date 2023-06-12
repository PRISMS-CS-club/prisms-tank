package org.prismsus.tank.utils.collidable

import org.prismsus.tank.utils.DVec2
import org.prismsus.tank.utils.treeDistinct

/**
* A ColMultiPart is a collection of ColPolys that are connected to each other.
* Any changes, including shifting and rotation, on any part of a ColMultiPart will affect the whole ColMultiPart.
* The [baseColPoly] is the main ColPoly that is used to locate the position of display in GUI
* The [subColPolys] are located by offset from the [baseColPoly] in GUI
 * [pts] in this class is the union of [baseColPoly] and [subColPolys]. So that when it is exposed
 * to the outside, it is considered as a whole. For example, when drawing this using [CoordPanel], the image is the union
 * [allPts] contains all the points in [baseColPoly], [subColPolys] and [pts]
 * For the point of same value in [baseColPoly], [subColPolys] and [pts], only one copy is kept in [allPts]
* */
class ColMultiPart(baseColPoly : ColPoly, vararg subColPolys : ColPoly) : ColPoly((baseColPoly.unionMultiple(*subColPolys))!!.pts){
    val baseColPoly = baseColPoly

    val allPts : Array<DPos2>

    val subColPolys : Array<ColPoly> = subColPolys.toList().toTypedArray()

    init{
        val tmpArr = ArrayList<DPos2>()
        tmpArr.addAll(baseColPoly.pts)
        subColPolys.forEach { tmpArr.addAll(it.pts) }
        tmpArr.addAll(pts)
        for (sub in subColPolys)
            tmpArr.add(sub.rCenter)
        tmpArr.add(baseColPoly.rCenter)
        tmpArr.add(rCenter)
        allPts = tmpArr.toTypedArray().treeDistinct().toTypedArray()
        baseColPoly.parentEle = this
        subColPolys.forEach { it.parentEle = this }
        checkRcenter()
    }

    fun checkRcenter() {
        var found = false
        for (pt in allPts) {
            if (pt === rCenter) {
                found = true
                break
            }
        }
        if (!found){
            assert(false)
        }
        found = false
        for (pt in allPts) {
            if (pt === baseColPoly.rCenter) {
                found = true
                break
            }
        }
        if (!found){
            assert(false)
        }
        found = false
        for (sub in subColPolys) {
            for (pt in allPts) {
                if (pt === sub.rCenter) {
                    found = true
                    break
                }
            }
            if (!found){
                assert(false)
            }
        }
    }

    /**
     * By overaloding this function, [rotate], [rotateTo], [rotateAssignTo] ... will be available to use
     * */
    override fun rotateAssign(radOffset: Double, center: DPos2): ColMultiPart {
        checkRcenter()
        angleRotated += radOffset
        for (pt in allPts) {
            pt.rotateAssign(radOffset, center)
        }
        for (sub in subColPolys) {
            sub.angleRotated += radOffset
        }
        baseColPoly.angleRotated += radOffset
        checkRcenter()
        return this
    }

    override fun plusAssign(shift: DVec2) {
        checkRcenter()
        for (pt in allPts) {
            pt.plusAssign(shift)
        }
        checkRcenter()
    }

//    override fun minusAssign(shift: DVec2) {
//        checkRcenter()
//        for (pt in allPts) {
//            pt.minusAssign(shift)
//        }
//        checkRcenter()
//    }
//
//    override fun plus(shift: DVec2): ColPoly {
//        checkRcenter()
//        val newBase = ColPoly(baseColPoly.pts.copyOf().map { it.copy().shift(shift) as DPos2}.toTypedArray())
//        val newSubs = subColPolys.map {
//            ColPoly(it.pts.copyOf().map { it.copy().shift(shift) as DPos2 }.toTypedArray())
//        }.toTypedArray()
//        val ret = ColMultiPart(newBase, *newSubs)
//        ret.angleRotated = angleRotated
//        checkRcenter()
//        return ret
//    }

    override fun becomeNonCopy(other: Collidable) {
        checkRcenter()
        if (other !is ColMultiPart) {
            throw IllegalArgumentException("ColMultiPart can only become ColMultiPart")
        }
        if (other === this) {
            return
        }
        angleRotated = other.angleRotated
        baseColPoly.becomeNonCopy(other.baseColPoly)
        allPts.forEachIndexed() { i, pt ->
            allPts[i] = other.allPts[i]
        }
        for (i in 0 until other.subColPolys.size) {
            subColPolys[i].becomeNonCopy(other.subColPolys[i])
        }
        rCenter = other.rCenter
        pts = other.pts.copyOf()
        checkRcenter()
    }

    override fun copy(): ColMultiPart{
        checkRcenter()
        val newBase = baseColPoly.copy() as ColPoly
        val newSubs = subColPolys.copyOf().map { it.copy() as ColPoly }.toTypedArray()
        val ret = ColMultiPart(newBase, *newSubs)
        ret.angleRotated = angleRotated
        checkRcenter()
        return ret
    }

    override var parentEle: ColMultiPart?
        get() = null
        set(value) {
            throw IllegalArgumentException("ColMultiPart cannot have parent")
        }
}