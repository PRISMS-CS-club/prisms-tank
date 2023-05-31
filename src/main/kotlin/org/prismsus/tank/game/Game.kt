package org.prismsus.tank.game

import org.prismsus.tank.bot.FutureController
import org.prismsus.tank.bot.GameBot
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.elements.GameMap
import org.prismsus.tank.elements.MovableElement
import org.prismsus.tank.elements.Tank
import org.prismsus.tank.event.ElementUpdateEvent
import org.prismsus.tank.event.GameEvent
import org.prismsus.tank.event.UpdateEventSlect
import java.util.LinkedList
import java.util.concurrent.PriorityBlockingQueue
import org.prismsus.tank.game.TankWeaponInfo.*
import org.prismsus.tank.game.OtherRequests.*
import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.collidable.DPos2

class Game(vararg val bots: GameBot<FutureController>) {
    var eventHistory = LinkedList<GameEvent>()
    lateinit var controllers: Array<FutureController>
    val requestsQ = PriorityBlockingQueue<ControllerRequest<Any>>()
    val map = GameMap("default.json")
    val cidToTank = mutableMapOf<Long, Tank>()

    init {
        controllers = Array(bots.size) { i -> FutureController(i.toLong(), requestsQ) }
        for ((i, c) in controllers.withIndex()) {
            val tankPos = map.getUnoccupiedRandPos(INIT_TANK_COLBOX)
            var uid = 0
            val tank = map.addEle(Tank(nextUid, INIT_RECT_WEAPON_RPOPS))
            tank.colPoly.rotationCenter = tankPos
            cidToTank[c.cid] = tank as Tank
        }

    }

    fun tankWeaponInfoHandler(req: ControllerRequest<Any>) : Any {
        when (req.requestType) {
            TANK_HP -> {
                return cidToTank[req.cid]!!.hp
            }

            TANK_MAX_HP -> {
                // TODO: add max hp field in tank
                return INIT_TANK_HP
            }

            TANK_LTRACK_SPEED -> {
                return cidToTank[req.cid]!!.leftTrackVelo
            }

            TANK_RTRACK_SPEED -> {
                return cidToTank[req.cid]!!.rightTrackVelo
            }

            TANK_TRACK_MAX_SPEED -> {
                return cidToTank[req.cid]!!.trackMaxSpeed
            }

            TANK_COLBOX -> {
                return cidToTank[req.cid]!!.rectBox
            }

            TANK_POS -> {
                return cidToTank[req.cid]!!.colPoly.rotationCenter
            }

            TANK_ANGLE -> {
                return cidToTank[req.cid]!!.colPoly.angleRotated
            }

            WEAPON_RELOAD_RATE_PER_SEC -> {
                return cidToTank[req.cid]!!.weapon.reloadRate
            }

            WEAPON_MAX_CAPACITY -> {
                return cidToTank[req.cid]!!.weapon.maxCapacity
            }

            WEAPON_CUR_CAPACITY -> {
                return cidToTank[req.cid]!!.weapon.curCapa
            }

            WEAPON_DAMAGE -> {
                return cidToTank[req.cid]!!.weapon.damage
            }

            WEAPON_COLBOX -> {
                return cidToTank[req.cid]!!.weapon.colPoly
            }

            COMBINED_COLBOX -> {
                return cidToTank[req.cid]!!.colPoly
            }

            BULLET_COLBOX -> {
                // TODO: add bullet colbox as a field in weapon
                return INIT_BULLET_COLBOX
            }

            BULLET_SPEED -> {
                // TODO: add bullet speed as a field in weapon
                return INIT_BULLET_SPEED
            }
            else -> {
                throw IllegalArgumentException("Invalid request type")
            }
        }
    }

    fun handleOtherRequests(req : ControllerRequest<Any>){
        when (req.requestType) {
            GET_VISIBLE_ELEMENTS -> {
                // TODO: implement limited visibility
                val ret = ArrayList(map.gameEles)
                req.returnTo!!.complete(ret)
            }

            GET_VISITED_ELEMENTS -> {
                // TODO: implement this
                req.returnTo!!.complete(ArrayList<GameElement>())
            }
            SHOOT -> {
                val bullet = cidToTank[req.cid]!!.weapon.fire()
                if (bullet != null)
                map.addEle(bullet)
            }
            SET_LTRACK_SPEED -> {
                val target = req.params!!.first() as Double
                cidToTank[req.cid]!!.leftTrackVelo = target
            }
            SET_RTRACK_SPEED -> {
                val target = req.params!!.first() as Double
                cidToTank[req.cid]!!.rightTrackVelo = target
            }
        }
    }

    fun start() {
        for ((i, bot) in bots.withIndex()) {
            Thread {
                bot.loop(controllers[i])
            }.start()
        }
        var lastUpd = System.currentTimeMillis()
        while (true) {
            synchronized(requestsQ) {
                // first handle all the requests, then move all the elements
                while (!requestsQ.isEmpty()) {
                    val curReq = requestsQ.poll()
                    when (curReq.requestType) {
                        is TankWeaponInfo -> {
                            val ret = tankWeaponInfoHandler(curReq)
                            curReq.returnTo!!.complete(ret)
                        }
                        is OtherRequests -> {
                            handleOtherRequests(curReq)
                        }
                        else -> {
                            throw IllegalArgumentException("Invalid request type")
                        }
                    }
                }
            }
            val dt = System.currentTimeMillis() - lastUpd
            lastUpd = System.currentTimeMillis()
            for (updatable in map.timeUpdatables){
                var prevCent : DPos2? = null
                if (updatable is MovableElement){
                    prevCent = updatable.colPoly.rotationCenter
                }
                updatable.updateByTime(dt)
                if (updatable is MovableElement){
                    val collideds = map.quadTree.collidedObjs(updatable.colPoly)
                    for (collided in collideds){
                        updatable.processCollision(map.collidableToEle[collided]!!)
                        map.collidableToEle[collided]!!.processCollision(updatable)
                    }
                    updatable.colPoly.rotationCenter = prevCent!!
                    eventHistory.add(ElementUpdateEvent(updatable,
                            UpdateEventSlect.defaultTrue()
                        ))
                }
            }
        }
    }
}