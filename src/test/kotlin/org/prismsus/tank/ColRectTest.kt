package org.prismsus.tank

import org.junit.jupiter.api.Test
import org.prismsus.tank.utils.DDim2
import org.prismsus.tank.utils.collidable.ColRect
import org.prismsus.tank.utils.collidable.DPos2
import java.util.Random

class ColRectTest{
    @Test
    fun rotate(){
        val rect = ColRect(DPos2(0, 0), DDim2(10, 10))
        for (i in 0..500000){
            val rotAng = Math.random() * 2 * Math.PI
            rect.rotateAssign(rotAng)
            rect.rotateAssign(-rotAng)
        }
        println(rect)
    }
}