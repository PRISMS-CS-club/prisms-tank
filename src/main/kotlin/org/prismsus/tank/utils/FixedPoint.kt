package org.prismsus.tank.utils

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.min
import kotlin.math.round

/**
 * Fixed point number representing `digit` times 10 to the power of `power`.
 */
class FixedPoint(private var number: Double, val digit: Int): Number() {
    init{
        number = BigDecimal(number).setScale(digit, RoundingMode.HALF_EVEN).toDouble()
    }
    override fun toByte(): Byte {
        return toLong().toByte()
    }

    override fun toChar(): Char {
        throw NotImplementedError("deprecated")
    }

    override fun toDouble(): Double {
        return number
    }

    override fun toFloat(): Float {
        return toDouble().toFloat()
    }

    override fun toInt(): Int {
        return toLong().toInt()
    }

    override fun toLong(): Long {
        return number.toLong()
    }

    override fun toShort(): Short {
        return toLong().toShort()
    }

    override fun toString(): String {
        return toDouble().toString()
    }

}