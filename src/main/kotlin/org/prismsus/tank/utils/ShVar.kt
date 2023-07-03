package org.prismsus.tank.utils

import org.prismsus.tank.elements.GameMap

/*
* shared variables
* */

@Volatile
var nextUid : Long = 0
    @Synchronized get() = field++
    private set
var gameMap : GameMap? = null