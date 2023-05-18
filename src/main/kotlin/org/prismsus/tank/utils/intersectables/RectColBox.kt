package org.prismsus.tank.utils.intersectables

import org.prismsus.tank.utils.DDim2
import org.prismsus.tank.utils.DVec2

class RectColBox (val centerPos : DPos2, val size : DDim2) : ColBox(arrayOf(
    centerPos + DVec2(-size.x / 2.0, size.y / 2.0),
    centerPos + size / 2.0,
    centerPos + DVec2(size.x / 2.0, -size.y / 2.0),
    centerPos - size / 2.0
)){
    companion object{
        fun byTopLeft(topLeft : DPos2, size : DDim2) : RectColBox {
            return RectColBox(topLeft + DVec2(size.x / 2, -size.y / 2), size)
        }
    }

    var topLeftPt : DPos2
        get() = pts[0]
        set(value){}
    var topRightPt : DPos2
        get() = pts[1]
        set(value){}
    var bottomRightPt : DPos2
        get() = pts[2]
        set(value){}
    var bottomLeftPt : DPos2
        get() = pts[3]
        set(value){}
    var leftMidPt : DPos2
        get() = ((topLeftPt.toVec() + bottomLeftPt.toVec()) / 2.0).toPt()
        set(value){}
    var rightMidPt : DPos2
        get() = ((topRightPt.toVec() + bottomRightPt.toVec()) / 2.0).toPt()
        set(value){}
    var topMidPt : DPos2
        get() = ((topLeftPt.toVec() + topRightPt.toVec()) / 2.0).toPt()
        set(value){}
    var bottomMidPt : DPos2
        get() = ((bottomLeftPt.toVec() + bottomRightPt.toVec()) / 2.0).toPt()
        set(value){}
    var height : Double
        get() {
            return size.y
        }
        set(value){}
    var width : Double
        get() {
            return size.x
        }
        set(value){}
}