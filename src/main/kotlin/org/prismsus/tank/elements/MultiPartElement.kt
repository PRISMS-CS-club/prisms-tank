package org.prismsus.tank.elements

import org.prismsus.tank.utils.DVec2
import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.collidable.DPos2

interface MultiPartElement {

    val subElements : ArrayList<SubGameElement> // eg, tank have one subElement, which is the gun
    val baseColPoly : ColPoly                   // the colpoly of the base element, every other colpoly is shifted from this
    val subColPolys : ArrayList<ColPoly>        // copoly from subElements + baseColpoly
    val overallColPoly : ColPoly                // union of all subColPolys, usually colpoly of that MultiPartElement
    infix fun shiftAll(offset : DVec2){
        subColPolys.forEach { it += offset }
        overallColPoly += offset
    }

    fun rotateAll(offset : DVec2, center : DPos2) {

    }
}