package org.prismsus.tank.utils.collidable

import org.prismsus.tank.utils.DDim2
import org.prismsus.tank.utils.DVec2

// TODO: More accurate ColRect representation
open class ColRect (val centerPos : DPos2, val size : DDim2) : ColPoly(arrayOf(
    centerPos + DVec2(-size.x / 2.0, size.y / 2.0),
    centerPos + size / 2.0,
    centerPos + DVec2(size.x / 2.0, -size.y / 2.0),
    centerPos - size / 2.0
)){
    companion object{
        fun byTopLeft(topLeft : DPos2, size : DDim2) : ColRect {
            return ColRect(topLeft + DVec2(size.x / 2, -size.y / 2), size)
        }
        fun byBottomLeft(bottomLeft : DPos2, size : DDim2) : ColRect {
            val tmp = ColRect(bottomLeft, size)
            tmp.bottomLeftPt = bottomLeft
            return tmp
        }
        fun byTopRight(topRight : DPos2, size : DDim2) : ColRect {
            val tmp = ColRect(topRight, size)
            tmp.topRightPt = topRight
            return tmp
        }
        fun byBottomRight(bottomRight : DPos2, size : DDim2) : ColRect {
            val tmp = ColRect(bottomRight, size)
            tmp.bottomRightPt = bottomRight
            return tmp
        }
    }

    var topLeftPt : DPos2
        get() = pts[0]
        set(value){
            val vec = value.toVec() - topLeftPt.toVec()
            pts.forEach { it += vec }
            centerPos += vec
        }
    var topRightPt : DPos2
        get() = pts[1]
        set(value){
            val vec = value.toVec() - topRightPt.toVec()
            pts.forEach { it += vec }
            centerPos += vec
        }
    var bottomRightPt : DPos2
        get() = pts[2]
        set(value){
            val vec = value.toVec() - bottomRightPt.toVec()
            pts.forEach { it += vec }
        }
    var bottomLeftPt : DPos2
        get() = pts[3]
        set(value){
            val vec = value.toVec() - bottomLeftPt.toVec()
            pts.forEach { it += vec }
            centerPos += vec
        }
    var leftMidPt : DPos2
        get() = ((topLeftPt.toVec() + bottomLeftPt.toVec()) / 2.0).toPt()
        set(value){
            val vec = value.toVec() - leftMidPt.toVec()
            pts.forEach { it += vec }
        }
    var rightMidPt : DPos2
        get() = ((topRightPt.toVec() + bottomRightPt.toVec()) / 2.0).toPt()
        set(value){
            val vec = value.toVec() - rightMidPt.toVec()
            pts.forEach { it += vec }
            centerPos += vec
        }
    var topMidPt : DPos2
        get() = ((topLeftPt.toVec() + topRightPt.toVec()) / 2.0).toPt()
        set(value){
            val vec = value.toVec() - topMidPt.toVec()
            pts.forEach { it += vec }
            centerPos += vec
        }
    var bottomMidPt : DPos2
        get() = ((bottomLeftPt.toVec() + bottomRightPt.toVec()) / 2.0).toPt()
        set(value){
            val vec = value.toVec() - bottomMidPt.toVec()
            pts.forEach { it += vec }
            centerPos += vec
        }

    override fun copy(): ColRect {
        return byTopLeft(topLeftPt, size)
    }
    override val height : Double
        get() {
            return size.y
        }
    override val width : Double
        get() {
            return size.x
        }
}