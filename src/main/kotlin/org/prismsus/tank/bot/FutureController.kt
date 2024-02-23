package org.prismsus.tank.bot

import org.prismsus.tank.elements.Block
import org.prismsus.tank.elements.Bullet
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.elements.Tank
import org.prismsus.tank.game.ControllerRequest
import org.prismsus.tank.game.OtherRequests
import org.prismsus.tank.game.TankWeaponInfo
import org.prismsus.tank.markets.AuctionUserInterface
import org.prismsus.tank.utils.IVec2
import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.collidable.ColRect
import org.prismsus.tank.utils.collidable.DPos2
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.PriorityBlockingQueue

class FutureController(
    val cid: Long,
    var requestsQ: PriorityBlockingQueue<ControllerRequest<Any>>,
    val market: AuctionUserInterface
) {
    private val curAPICallCnt = 114514
    var threadCount: Int = 0
        private set

    fun createThread(th: Runnable): Thread? {
        if (threadCount >= MAX_THREAD) return null
        threadCount++
        return Thread(th)
    }


    val visibleElements: Future<List<GameElement>>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, OtherRequests.GET_VISIBLE_ELEMENTS))
            return ret.thenApply { it: Any -> it as List<GameElement> }
        }

    val visibleTanks: Future<List<Tank>>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, OtherRequests.GET_VISIBLE_TANKS))
            return ret.thenApply { it: Any -> it as List<Tank> }
        }

    val visibleBullets: Future<List<Bullet>>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, OtherRequests.GET_VISIBLE_BULLETS))
            return ret.thenApply { it: Any -> it as List<Bullet> }
        }

    val visitedElements: Future<List<GameElement>>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, OtherRequests.GET_VISITED_ELEMENTS))
            return ret.thenApply { it: Any -> it as List<GameElement> }
        }

    fun checkBlockAt(pos: IVec2): Future<Block> {
        val ret = CompletableFuture<Any>()

        requestsQ.add(ControllerRequest(cid, ret, OtherRequests.CHECK_BLOCK_AT, arrayOf(pos)))
        // convert type if non-null
        return ret.thenApply { it: Any ->
           return@thenApply it as Block
        }
    }

    fun checkCollidingGameEles(): Future<ArrayList<GameElement>> {
        val ret = CompletableFuture<Any>()
        requestsQ.add(ControllerRequest(cid, ret, OtherRequests.CHECK_COLLIDING_GAME_ELES))
        return ret.thenApply { it: Any ->
            return@thenApply it as ArrayList<GameElement>
        }
    }

    fun fire() {
        requestsQ.add(ControllerRequest(cid, null, OtherRequests.FIRE))
    }

    fun getTankAndWeaponInfos(vararg type: TankWeaponInfo): Future<ArrayList<*>> {
        val ret = CompletableFuture<Any>()
        for (t in type) {
            requestsQ.add(ControllerRequest(cid, ret, t))
        }
        val test: ControllerRequest<*> = requestsQ.peek()
        return ret.thenApply { it: Any -> it as ArrayList<*> }
    }

    val tankHp: Future<Int>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.TANK_HP))
            return ret.thenApply { it: Any -> it as Int }
        }


    val tankMaxHp: Future<Int>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.TANK_MAX_HP))
            return ret.thenApply { it: Any -> it as Int }
        }

    val tankLeftTrackSpeed: Future<Double>
        get() {
            // convert using thenApply
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.TANK_LTRACK_SPEED))
            return ret.thenApply { it: Any -> it as Double }
        }

    val tankRightTrackSpeed: Future<Double>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.TANK_RTRACK_SPEED))
            return ret.thenApply { it: Any -> it as Double }
        }

    val tankTrackMaxSpeed: Future<Double>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.TANK_TRACK_MAX_SPEED))
            return ret.thenApply { it: Any -> it as Double }
        }

    val tankColBox: Future<ColRect>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.TANK_COLBOX))
            return ret.thenApply { it: Any -> it as ColRect }
        }

    val tankPos: Future<DPos2>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.TANK_POS))
            return ret.thenApply { it: Any -> it as DPos2 }
        }

    val tankAngle: Future<Double>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.TANK_ANGLE))
            return ret.thenApply { it: Any -> it as Double }
        }

    val weaponReloadTimePerSecond: Future<Double>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.WEAPON_RELOAD_RATE_PER_SEC))
            return ret.thenApply { it: Any -> it as Double }
        }

    val weaponCurCapacity: Future<Int>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.WEAPON_CUR_CAPACITY))
            return ret.thenApply { it: Any -> it as Int }
        }

    val weaponMaxCapacity: Future<Int>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.WEAPON_MAX_CAPACITY))
            return ret.thenApply { it: Any -> it as Int }
        }

    val weaponColBox: Future<ColRect>
        // weapon colbox
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.WEAPON_COLBOX))
            return ret.thenApply { it: Any -> it as ColRect }
        }

    val combinedColBox: Future<ColPoly>
        // combined colbox
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.COMBINED_COLBOX))
            return ret.thenApply { it: Any -> it as ColPoly }
        }

    val bulletColBox: Future<ColRect>
        // bullet colbox
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.BULLET_COLBOX))
            return ret.thenApply { it: Any -> it as ColRect }
        }

    val bulletSpeed: Future<Double>
        // bullet speed
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.BULLET_SPEED))
            return ret.thenApply { it: Any -> it as Double }
        }

    val tankVisibleRange: Future<Double>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, TankWeaponInfo.TANK_VIS_RANGE))
            return ret.thenApply { it: Any -> it as Double }
        }

    fun setDebugString(str: String) {
        requestsQ.add(ControllerRequest(cid, null, OtherRequests.SET_DEBUG_STRING, arrayOf(str)))
    }

    val hpMoneyIncRate: Future<Pair<Double, Double>>
        get() {
            val ret = CompletableFuture<Any>()
            requestsQ.add(ControllerRequest(cid, ret, OtherRequests.GET_HP_MONEY_INC_RATE))
            return ret.thenApply { it: Any -> it as Pair<Double, Double> }
        }


    fun setLeftTrackSpeed(speed: Double) {
        requestsQ.add(ControllerRequest(cid, null, OtherRequests.SET_LTRACK_SPEED, arrayOf<Double>(speed)))
    }

    fun setRightTrackSpeed(speed: Double) {
        requestsQ.add(ControllerRequest(cid, null, OtherRequests.SET_RTRACK_SPEED, arrayOf<Double>(speed)))
    }

    companion object {
        const val MAX_THREAD: Int = 4
    }
}
