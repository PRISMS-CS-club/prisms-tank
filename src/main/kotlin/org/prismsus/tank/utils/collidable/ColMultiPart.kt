package org.prismsus.tank.utils.collidable

import org.prismsus.tank.utils.DVec2
import org.prismsus.tank.utils.treeDistinct
import java.awt.Shape



class ColMultiPart(val baseColPoly : ColPoly, vararg subColPolys : ColPoly) : ColPoly((baseColPoly.unionMultiple(*subColPolys))!!.pts){
    val allPts : Array<DPos2>
    val subColPolys : Array<ColPoly> = subColPolys.toList().toTypedArray()
    init{
        val tmpArr = ArrayList<DPos2>()
        tmpArr.addAll(baseColPoly.pts)
        subColPolys.forEach { tmpArr.addAll(it.pts) }
        tmpArr.addAll(pts)
        allPts = tmpArr.toTypedArray().treeDistinct().toTypedArray()
        baseColPoly.parentEle = this
        subColPolys.forEach { it.parentEle = this }
    }

    override fun rotateAssign(radOffset: Double, center: DPos2): ColMultiPart {
        angleRotated += radOffset
        for (pt in allPts) {
            pt.rotateAssign(radOffset, center)
        }
        return this
    }

    override fun plusAssign(shift: DVec2) {
        for (pt in allPts) {
            pt.plusAssign(shift)
        }
    }

    override fun minusAssign(shift: DVec2) {
        for (pt in allPts) {
            pt.minusAssign(shift)
        }
    }

    override fun plus(shift: DVec2): ColPoly {
        val newBase = ColPoly(baseColPoly.pts.copyOf().map { it.copy().shift(shift) as DPos2}.toTypedArray())
        val newSubs = subColPolys.map {
            ColPoly(it.pts.copyOf().map { it.copy().shift(shift) as DPos2 }.toTypedArray())
        }.toTypedArray()
        val ret = ColMultiPart(newBase, *newSubs)
        ret.angleRotated = angleRotated
        return ret
    }

    override fun minus(shift: DVec2): ColPoly {
        return plus(-shift)
    }


    override fun become(other: Collidable) {
        if (other !is ColMultiPart) {
            throw IllegalArgumentException("ColMultiPart can only become ColMultiPart")
        }
        if (other === this) {
            return
        }
        angleRotated = other.angleRotated
        baseColPoly.become(other.baseColPoly)
        for (i in 0 until other.subColPolys.size) {
            subColPolys[i].become(other.subColPolys[i])
        }
        pts = other.pts.copyOf()
    }

    override fun copy(): ColMultiPart{
        val newBase = baseColPoly.copy() as ColPoly
        if (newBase != baseColPoly){
            // TODO: Find out the place of inaccuracy issue
            println(newBase)
            println(baseColPoly)
//            assert(newBase == baseColPoly)
            println(baseColPoly.copy())
            throw Exception("newBase != baseColPoly")
        }
        val newSubs = subColPolys.copyOf().map { it.copy() as ColPoly }.toTypedArray()
        newSubs.forEachIndexed{i, it -> assert(it == subColPolys[i])}
        val ret = ColMultiPart(newBase, *newSubs)
        ret.angleRotated = angleRotated
        return ret
    }

    override var parentEle: ColMultiPart?
        get() = null
        set(value) {
            throw IllegalArgumentException("ColMultiPart cannot have parent")
        }
}