package org.prismsus.tank.bot

import org.prismsus.tank.utils.toModAngle
import org.prismsus.tank.utils.toModPosAngle

class TankAimingBot : GameBot {
    lateinit var c: FutureController

    private fun turnLeftInPlace() {
        c.setLeftTrackSpeed(-1.0)
        c.setRightTrackSpeed(1.0)
    }

    private fun turnRightInPlace() {
        c.setLeftTrackSpeed(1.0)
        c.setRightTrackSpeed(-1.0)
    }

    private fun halt() {
        c.setLeftTrackSpeed(0.0)
        c.setRightTrackSpeed(0.0)
    }

    private fun turnUntilThenFire(rad: Double) {
        val ANG_EPS = .05
        val curRad = c.tankAngle.get().toModAngle()

        if (Math.abs(curRad - rad) < ANG_EPS) {
            c.fire()
            halt()
            return
        }

        if (curRad < rad) {
            turnLeftInPlace()
        } else if (curRad > rad) {
            turnRightInPlace()
        }
    }

    override fun loop(_c: FutureController) {
        c = _c
        try {
            while (!Thread.interrupted()) {
                val tks = c.visibleTanks.get()
                if (tks.isEmpty()) {
                    halt()
                    continue
                }
                var mnAng = Double.MAX_VALUE
                for (tk in tks) {
                    val tkRotCent = tk.colPoly.rotationCenter
                    val curCent = c.tankPos.get()
                    val vec = tkRotCent.toVec() - curCent.toVec()
                    val ang = vec.angle()
                    mnAng = Math.min(mnAng, ang)
                }
                turnUntilThenFire(mnAng)
            }
        } catch (e: InterruptedException) {
            return
        }
    }

    override fun getName(): String {
        return "TankAimingBot"
    }

    override fun isFutureController(): Boolean {
        return true
    }
}