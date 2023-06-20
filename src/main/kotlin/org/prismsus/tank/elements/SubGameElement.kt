package org.prismsus.tank.elements

import org.prismsus.tank.utils.DVec2
import org.prismsus.tank.utils.collidable.ColPoly
interface SubGameElement {
    val serialName : String  // the serial name of this sub element
    val colPoly : ColPoly      // the collision box of this sub element
    var offsetFromParentCenter : DVec2
    // The offset of the [ColPoly.rotationalCenter] of this sub element to the rotational center of the parent
    // This value is relative, if yoffset is 1, it means that the rotational center of this SubGameElement will
    // be located at the parent's maximum y value
    var belongTo : GameElement      // the uid of the parent game element
    // the later two variables are var
    // since they could be set in the parent game element
}
