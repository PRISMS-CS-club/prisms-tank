package org.prismsus.tank.element

import org.prismsus.tank.utils.DVec2
import org.prismsus.tank.utils.collidable.ColBox

interface SubGameElement {
    val serialName : String  // the serial name of this sub element
    val colBox : ColBox      // the collision box of this sub element
    var centerOffset : DVec2 // the offset of the center of this sub element to the center of the parent game element
    // if null, this will be determined by the parent game element
    var belongTo : GameElement      // the uid of the parent game element
    // the later two variables are var
    // since they could be setted in the parent game element
}
