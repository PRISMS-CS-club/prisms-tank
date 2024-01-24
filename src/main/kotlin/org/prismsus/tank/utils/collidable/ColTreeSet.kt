package org.prismsus.tank.utils.collidable

import org.prismsus.tank.utils.*
import java.awt.Color
import java.awt.Shape
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * collection of collidables, implemented by a quad tree
 *
 */
class ColTreeSet(val dep: Int, val bound: ColAARect) {
    companion object {
        const val MAX_OBJECT = 4
        const val MAX_DEP = 8
    }

    var cols = ArrayList<Collidable>()
    var subTrees: Array<ColTreeSet>? = null
    var size = 0
    // indexed by quadrant
    var topLeftSub: ColTreeSet?
        get() = subTrees?.get(1)
        set(value) {
            value?.let { subTrees?.set(1, it) }
        }
    var topRightSub: ColTreeSet?
        get() = subTrees?.get(0)
        set(value) {
            value?.let { subTrees?.set(0, it) }
        }
    var bottomRightSub: ColTreeSet?
        get() = subTrees?.get(3)
        set(value) {
            value?.let { subTrees?.set(3, it) }
        }
    var bottomLeftSub: ColTreeSet?
        get() = subTrees?.get(2)
        set(value) {
            value?.let { subTrees?.set(2, it) }
        }
    val allSubCols: ArrayList<Collidable>
        get() {
            val res: ArrayList<Collidable> = cols.clone() as ArrayList<Collidable>
            subTrees?.forEach { res.addAll(it.allSubCols) }
            if(res.size != size)
                assert(res.size == size) { "allSubCols=${res.size}, size=$size" }
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
    fun checkColsInBound() : Boolean{
        cols.forEach {
            if (!(bound enclose it)){
                return false
//                assert(false)
            }
        }
        var ret = true
        subTrees?.forEach {
            if (!it.checkColsInBound()){
                ret = false
            }
        }
        return ret
    }

    /**
     * delete all the collidables in this subtree, including the collidables stored in
     * this node and the collidables stored in the subtrees.
     * */
    fun clearAll() {
        cols.clear()
        for (i in 0..3) {
            subTrees?.get(i)?.clearAll()
        }
        subTrees = null
    }

    /**
     * When the objects list in this node exceed the max object number, split it into four subtrees and move objects that
     * are bounded by a subtree into the subtrees.
     */
    private fun split() {
        if(subTrees != null) {
            // only split when this node have not been split
            return
        }
        // make each of the four subtrees slightly larger than bound.size / 2
        // so that there will be no gap between the four subtrees
        val offset = DOUBLE_PRECISION * 100.0
        val tlShift = DVec2(offset, offset) * 10.0
        val subDim = (bound.size / 2.0)
        val topLeft = bound.topLeftPt - tlShift.xVec + tlShift.yVec
        val quad2 = ColTreeSet(dep + 1, ColAARect.byTopLeft(topLeft, subDim + tlShift * 2.0))
        val quad1 = ColTreeSet(dep + 1, ColAARect.byTopLeft(topLeft + (subDim.xVec - tlShift.xVec), subDim + tlShift * 2.0 ))
        val quad4 = ColTreeSet(dep + 1, ColAARect.byTopLeft(topLeft - (subDim.yVec - tlShift.yVec) + (subDim.xVec - tlShift.xVec), subDim + tlShift * 2.0))
        val quad3 = ColTreeSet(dep + 1, ColAARect.byTopLeft(topLeft - (subDim.yVec - tlShift.yVec), subDim + tlShift * 2.0 ))
        subTrees = arrayOf(quad1, quad2, quad3, quad4)
        val toRemove = ArrayList<Collidable>()
        for (c in cols) {
            val belongTo = subTreeBelongTo(c.encAARect)
            if (belongTo != null) {
                assert(belongTo.insert(c))
                toRemove.add(c)
            }
        }
        for (c in toRemove) {
            cols.remove(c)
        }
    }

    /**
     * When the total number of objects in this node is less than max object number, merge the subtrees into one and move
     * all objects in the subtree back to this node.
     */
    private fun merge() {
        if(subTrees == null) {
            return
        }
        subTrees!![0].merge()
        subTrees!![1].merge()
        subTrees!![2].merge()
        subTrees!![3].merge()
        cols.addAll(subTrees!![0].cols)
        cols.addAll(subTrees!![1].cols)
        cols.addAll(subTrees!![2].cols)
        cols.addAll(subTrees!![3].cols)
        subTrees = null
    }

    /**
     *  calculate which subtree(quadrant) the AARect can be put in
     *  if none of the subtrees can completely contain the AARect or this node have not been splitted, return null
     * */
    fun subTreeBelongTo(box: ColAARect): ColTreeSet? {
        return subTrees?.let {
            for (sub in it) {
                if (sub.bound.enclose(box)) {
                    return sub
                }
            }
            return null
        }
    }

    /**
     * Insert a new collidable object into the quad-tree.
     * @param col The object to be inserted.
     * @return true if the insertion is successful, false otherwise.
     */
    fun insert(col: Collidable) : Boolean {
        if (!(bound enclose col))
            return false
        val belongTo = subTreeBelongTo(col.encAARect)
        if (belongTo != null) {
            assert(belongTo.insert(col))
            size++
            return true
        }
        cols.add(col)
        size++
        if(this.subTrees == null && cols.size > MAX_OBJECT && dep < MAX_DEP) {
            split()
        }
        return true
    }

    fun corespondingAARect(col : Collidable) : ColAARect{
        val belongTo = subTreeBelongTo(col.encAARect)
        if (belongTo != null) {
            return belongTo.corespondingAARect(col)
        }
        return bound
    }

    /**
     * Remove one collidable object from the collision quad-tree.
     * @param col The object to be removed.
     * @return True if the object is successfully removed, false if the object is not found.
     */
    fun remove(col: Collidable): Boolean {
        val belongTo = subTreeBelongTo(col.encAARect)
        if (belongTo != null) {
            if(!belongTo.remove(col)) {
                println("remove failed")
                throw Exception()
            }
            size--
            if(size <= MAX_OBJECT) {
                merge()
            }
            return true
        }
        if (cols.contains(col)) {
            cols.remove(col)
            size--
            if(size <= MAX_OBJECT) {
                merge()
            }
            return true
        }
        return false
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
        ret.addAll(cols) // all unsplittable collidable objects
        if (belongTo != null) {
            ret.addAll(belongTo.possibleCollision(col))
        } else {
        // add all the cols in the smallest square that can fit the col
            ret.clear()
            ret.addAll(allSubCols)
        }
        return ret
    }

    infix fun collide(col: Collidable): Boolean {
        return collidedObjs(col).isNotEmpty()
    }

    infix fun collidedObjs(col: Collidable): ArrayList<Collidable> {
        val possible = possibleCollision(col)
        val ret = ArrayList<Collidable>()
        for (c in possible) {
            if (c.collide(col) && c != col) {
                ret.add(c)
            }
        }
        return ret
    }


    fun getCoordinatePanel(panelSiz : IDim2) : CoordPanel {
        // first load all the collidable elements once, to avoid busy computations later
        val allCollidableElements: ArrayList<Collidable>
        val allPartitionLines: ArrayList<Line>
        synchronized(this) {
            allCollidableElements = allSubCols
            allPartitionLines = allSubPartitionLines
        }
        // find the max and min position of all collidable elements, to determine the size of the panel
        var maxPos = DPos2(Double.MIN_VALUE / 2, Double.MIN_VALUE / 2)
        var minPos = DPos2(Double.MAX_VALUE / 2, Double.MAX_VALUE / 2)
        for (col in allCollidableElements) {
            maxPos = maxPos max col.encAARect.topRightPt
            minPos = minPos min col.encAARect.bottomLeftPt
        }
        val xsz = max(abs(maxPos.x), abs(minPos.x)) * 1.5
        val ysz = max(abs(maxPos.y), abs(minPos.y)) * 1.5
        val pFactor = min(panelSiz.x / xsz, panelSiz.y / ysz)
        // make sure that the actual interval between grids is at least 30 pixel
        // actual interval = pinterv * pfactor
        val pInterv = ceil(max(30.0 / pFactor, 1.0)).toInt()
        val panel = CoordPanel(IDim2(pInterv, pInterv), IDim2(pFactor.toInt(), pFactor.toInt()), panelSiz, -panelSiz / 3)
        for (col in allCollidableElements){
            panel.drawCollidable(col)
        }
        for (line in allPartitionLines){
            panel.graphicsModifier = {
                g -> g.color = Color.RED
            }
            panel.drawCollidable(line)
        }
        return panel
    }
}