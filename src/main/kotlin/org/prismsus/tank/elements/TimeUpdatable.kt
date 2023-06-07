package org.prismsus.tank.elements

interface TimeUpdatable {
    fun updateByTime(dt: Long) // dt have a unit of ms
}