package org.prismsus.tank.utils

import kotlin.math.min
import kotlin.math.round

/**
 * Fixed point number representing `digit` times 10 to the power of `power`.
 */
class FixedPoint(val digit: Long, val power: Int): Number() {
    /**
     * Create a fixed point number from a [Double] number.
     * @param number The number to be converted.
     * @param point The number of digits after the decimal point.
     */
    constructor(number: Double, point: Int): this(round(number * powerOf10(point)).toLong(), -point)
    override fun toByte(): Byte {
        return toLong().toByte()
    }

    override fun toChar(): Char {
        TODO("deprecated")
    }

    override fun toDouble(): Double {
        return if(power > 0) {
            digit.toDouble() * powerOf10(power)
        } else if(power == 0) {
            digit.toDouble()
        } else {
            digit.toDouble() / powerOf10(-power)
        }
    }

    override fun toFloat(): Float {
        return if(power > 0) {
            digit.toFloat() * powerOf10(power)
        } else if(power == 0) {
            digit.toFloat()
        } else {
            digit.toFloat() / powerOf10(-power)
        }
    }

    override fun toInt(): Int {
        return toLong().toInt()
    }

    override fun toLong(): Long {
        return if(power > 0) {
            digit * powerOf10(power)
        } else if(power == 0) {
            digit
        } else {
            digit / powerOf10(-power)
        }
    }

    override fun toShort(): Short {
        return toLong().toShort()
    }

    override fun toString(): String {
        val sb = StringBuilder()
        val str = digit.toString()
        if(str.length + power <= 0) {
            sb.append("0.")
            for(i in 0 until -(str.length + power)) {
                sb.append('0')
            }
            sb.append(str)
        } else {
            sb.append(str.substring(0, min(str.length + power, str.length)))
            if(power > 0) {
                for(i in 0 until power) {
                    sb.append('0')
                }
            } else if(power < 0) {
                sb.append('.')
                sb.append(str.substring(str.length + power))
            }
        }
        return sb.toString()
    }

    companion object {
        /**
         * Compute nth power of 10. n must be a positive integer and less than 19.
         * @param n power
         * @return 10^n
         */
        fun powerOf10(n: Int): Long {
            return when(n) {
                0 -> 1L
                1 -> 10L
                2 -> 100L
                3 -> 1000L
                4 -> 10000L
                5 -> 100000L
                6 -> 1000000L
                7 -> 10000000L
                8 -> 100000000L
                9 -> 1000000000L
                10 -> 10000000000L
                11 -> 100000000000L
                12 -> 1000000000000L
                13 -> 10000000000000L
                14 -> 100000000000000L
                15 -> 1000000000000000L
                16 -> 10000000000000000L
                17 -> 100000000000000000L
                18 -> 1000000000000000000L
                else -> throw IllegalArgumentException("n must be less than 19")
            }
        }
    }
}