package org.prismsus.tank.utils

/*
* shared variables
* */

var nextUid : Long = 0
    @Synchronized get() = field++
    private set