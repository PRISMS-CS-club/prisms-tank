package org.prismsus.tank.utils.collidable

import org.prismsus.tank.utils.*
import java.awt.Shape

/**
 * collection of collidables, implemented by a quad tree
 *
 * */
class ColTreeSet(val dep: Int, val bound: ColAArect) {
    var MAX_OBJECT = 5
    var MAX_DEP = 8
    var cols = ArrayList<Collidable>()
    var subTrees: Array<ColTreeSet>? = null

    // indexed by quadrant
    var topLeftSub: ColTreeSet
        get() = subTrees?.get(1)!!
        set(value) {
            subTrees?.set(1, value)
        }
    var topRightSub: ColTreeSet
        get() = subTrees?.get(0)!!
        set(value) {
            subTrees?.set(0, value)
        }
    var bottomRightSub: ColTreeSet
        get() = subTrees?.get(3)!!
        set(value) {
            subTrees?.set(3, value)
        }
    var bottomLeftSub: ColTreeSet
        get() = subTrees?.get(2)!!
        set(value) {
            subTrees?.set(2, value)
        }
    val allSubCols: ArrayList<Collidable>
        get() {
            val res = ArrayList<Collidable>()
            subTrees?.forEach { res.addAll(it.allSubCols) }
            res.addAll(cols)
            return res
        }
    val allSubPartitionLines: ArrayList<Line>
        get() {
            val ret = ArrayList<Line>()
            subTrees?.forEach { ret.addAll(it.allSubPartitionLines) }

            ret.add(Line(bound.topLeftPt, bound.topRightPt))
            ret.add(Line(bound.topRightPt, bound.bottomRightPt))
            ret.add(Line(bound.bottomRightPt, bound.bottomLeftPt))
            ret.add(Line(bound.bottomLeftPt, bound.topLeftPt))

            return ret
        }

    /**
     * delete all the collidables in the tree, including the subtrees
     * */
    fun clearAll() {
        cols.clear()
        for (i in 0..3) {
            subTrees!![i].clearAll()
        }
        subTrees = null
    }

    /**
     * When one node exceeds the max object number, split it into four subtrees
     * */
    fun split() {
        // make each of the four subtrees slightly larger than bound.size / 2
        // so that there will be no gap between the four subtrees
        val tlShift = DVec2(DOUBLE_PRECISION, DOUBLE_PRECISION) * 10.0
        val subDim = (bound.size / 2.0) + tlShift * 2.0
        val topLeft = bound.topLeftPt - tlShift.xVec + tlShift.yVec
        val quad2 = ColTreeSet(dep + 1, ColAArect.byTopLeft(topLeft, subDim))
        val quad1 = ColTreeSet(dep + 1, ColAArect.byTopLeft(topLeft + subDim.xVec, subDim))
        val quad4 = ColTreeSet(dep + 1, ColAArect.byTopLeft(topLeft - subDim.yVec + subDim.xVec, subDim))
        val quad3 = ColTreeSet(dep + 1, ColAArect.byTopLeft(topLeft - subDim.yVec, subDim))
        subTrees = arrayOf(quad1, quad2, quad3, quad4)
    }


    /**
     *  calculate wich subtree(quadrant) the AArect can be put in
     *  if none of the subtrees can completely contain the AArect or this node have not been splitted, return null
     * */
    fun subTreeBelongTo(box: ColAArect): ColTreeSet? {
        if (subTrees == null) return null
        for (sub in subTrees!!) {
            if (sub.bound.enclose(box)) {
                return sub
            }
        }
        return null
    }

    fun insert(col: Collidable) {

        val belongTo = subTreeBelongTo(col.encAARect)
        if (belongTo != null) {
            belongTo.insert(col)
            return
        }

        cols.add(col)
        if (cols.size > MAX_OBJECT && dep < MAX_DEP) {
            split()
            val toRemove = ArrayList<Collidable>()
            for (c in cols) {
                val belongTo = subTreeBelongTo(c.encAARect)
                if (belongTo != null) {
                    belongTo.insert(c)
                    toRemove.add(c)
                }
            }
            for (c in toRemove) {
                cols.remove(c)
            }
        }
    }

    fun corespondingAArect(col : Collidable) : ColAArect{
        val belongTo = subTreeBelongTo(col.encAARect)
        if (belongTo != null) {
            return belongTo.corespondingAArect(col)
        }
        return bound
    }

    fun remove(col: Collidable) {
        if (subTrees != null) {
            val belongTo = subTreeBelongTo(col.encAARect)
            belongTo?.remove(col)
            if (belongTo != null) {
                return
            }
        }
        if (!cols.contains(col))
            throw Exception("ColTreeSet.remove: the collidable to be removed is not in the tree")
        cols.remove(col)
    }

    fun toShapes(coordTransform: (DPos2) -> DPos2 = { it }, shapeModifier: (Shape) -> Unit = { it }): ArrayList<Shape> {
        val ret = ArrayList<Shape>()
        for (col in allSubCols) {
            val shape = col.toShape(coordTransform)
            shapeModifier(shape)
            ret.add(shape)
        }
        return ret
    }


    infix fun possibleCollision(col: Collidable): ArrayList<Collidable> {
        val belongTo = subTreeBelongTo(col.encAARect)
        val ret = ArrayList<Collidable>()
        ret.addAll(cols) // all unsplittable collidables
        if (belongTo != null) {
            ret.addAll(belongTo.possibleCollision(col))
        }
        return ret
    }

    infix fun collide(col: Collidable): Boolean {
        val possible = possibleCollision(col)
        return collidedObjs(col).isNotEmpty()
    }

    infix fun collidedObjs(col: Collidable): ArrayList<Collidable> {
        val possible = possibleCollision(col)
        val ret = ArrayList<Collidable>()
        for (c in possible) {
            if (c.collide(col)) {
                ret.add(c)
            }
        }
        return ret
    }
}