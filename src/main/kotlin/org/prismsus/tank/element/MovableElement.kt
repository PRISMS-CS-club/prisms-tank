package org.prismsus.tank.element

import org.prismsus.tank.utils.ColBox
import org.prismsus.tank.utils.DVec2

abstract class MovableElement(uid: Long, hp: Int, colBox: ColBox, val maxTrackSpeed : Double) :
    GameElement(uid, hp, colBox){
    }