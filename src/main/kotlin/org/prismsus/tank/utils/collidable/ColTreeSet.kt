package org.prismsus.tank.utils.collidable

import org.prismsus.tank.utils.*

/**
 * collection of collidables, implemented by a quad tree
 *
 * */
class ColTreeSet(val dep: Int, val bound: ColAARect) {
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

    /**
     * delete all the collidables in the tree, including the subtrees
     * */
    fun clear() {
        cols.clear()
        for (i in 0..3) {
            subTrees!![i].clear()
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
        val subDim = bound.size / 2.0 + tlShift
        val topLeft = bound.topLeftPt - tlShift
        val quad2 = ColTreeSet(dep + 1, ColAARect.byTopLeft(topLeft, subDim))
        val quad1 = ColTreeSet(dep + 1, ColAARect.byTopLeft(topLeft + DVec2(subDim.x, 0.0), subDim))
        val quad4 = ColTreeSet(dep + 1, ColAARect.byTopLeft(topLeft + subDim, subDim))
        val quad3 = ColTreeSet(dep + 1, ColAARect.byTopLeft(topLeft + DVec2(0.0, subDim.y), subDim))
        subTrees = arrayOf(quad1, quad2, quad3, quad4)
    }


    /**
     *  calculate wich subtree(quadrant) the AArect can be put in
     *  if none of the subtrees can completely contain the AArect or this node have not been splitted, return null
     * */
    fun subTreeBelongTo(box: ColAARect): ColTreeSet? {
        if (subTrees == null) return null
        for (sub in subTrees!!) {
            if (sub.bound.enclose(box)) {
                return sub
            }
        }
        return null
    }

    fun insert(col: Collidable) {
        if (subTrees != null) {
            val belongTo = subTreeBelongTo(col.encAARect)
            belongTo?.insert(col)
            if (belongTo != null) {
                return
            }
        }
        cols.add(col)
        if (cols.size > MAX_OBJECT && dep < MAX_DEP) {
            split()
            val toRemove = ArrayList<Collidable>()
            for (c in cols){
                val belongTo = subTreeBelongTo(c.encAARect)
                if (belongTo != null) {
                    belongTo.insert(c)
                    toRemove.add(c)
                }
            }
            for (c in toRemove){
                cols.remove(c)
            }
        }
    }

    infix fun possibleCollision(col : Collidable) : ArrayList<Collidable>{
        val belongTo = subTreeBelongTo(col.encAARect)
        val ret = ArrayList<Collidable>()
        ret.addAll(cols) // all unsplittable collidables
        if (belongTo != null) {
            ret.addAll(belongTo.possibleCollision(col))
        }
        return ret
    }
}