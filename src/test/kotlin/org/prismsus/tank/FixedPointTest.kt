package org.prismsus.tank

import org.junit.jupiter.api.Test
import org.prismsus.tank.utils.FixedPoint
import kotlin.test.assertEquals

class FixedPointTest {
    @Test
    fun string() {
//        val fp = FixedPoint(123456789, -3)
//        assertEquals(fp.toString(), "123456.789")
//        val fp2 = FixedPoint(123456789, 3)
//        assertEquals(fp2.toString(), "123456789000")
//        val fp3 = FixedPoint(123456789, 0)
//        assertEquals(fp3.toString(), "123456789")
//        val fp4 = FixedPoint(123456789, -6)
//        assertEquals(fp4.toString(), "123.456789")
//        val fp5 = FixedPoint(123456789, 6)
//        assertEquals(fp5.toString(), "123456789000000")
//        val fp6 = FixedPoint(1234, -6)
//        assertEquals(fp6.toString(), "0.001234")

        val fp7 = FixedPoint(123.45678, 3)
        assertEquals(fp7.toString(), "123.457")
        val fp8 = FixedPoint(123.45678, 2)
        assertEquals(fp8.toString(), "123.46")
        val fp9 = FixedPoint(123.45678, 1)
        assertEquals(fp9.toString(), "123.5")
        val fp10 = FixedPoint(0.114514, 2)
        assertEquals(fp10.toString(), "0.11")
    }
}