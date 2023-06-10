package org.prismsus.tank.game

import org.prismsus.tank.bot.FutureController
import org.prismsus.tank.bot.GameBot
import org.prismsus.tank.bot.RandomMovingBot
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.elements.GameMap
import org.prismsus.tank.elements.MovableElement
import org.prismsus.tank.elements.Tank
import org.prismsus.tank.event.ElementCreateEvent
import org.prismsus.tank.event.ElementUpdateEvent
import org.prismsus.tank.event.GameEvent
import org.prismsus.tank.event.UpdateEventMask
import org.prismsus.tank.game.OtherRequests.*
import org.prismsus.tank.game.TankWeaponInfo.*
import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.ColMultiPart
import org.prismsus.tank.utils.collidable.DPos2
import java.io.File
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.PriorityBlockingQueue

class Game(val replayFile: File, vararg val bots: GameBot<FutureController>) {
    val eventHistory = PriorityBlockingQueue<GameEvent>()
    lateinit var controllers: Array<FutureController>
    val requestsQ = PriorityBlockingQueue<ControllerRequest<Any>>()
    val map = GameMap("default.json")
    val cidToTank = mutableMapOf<Long, Tank>()
    val botThs: Array<Thread?> = Array(bots.size) { null }
    lateinit var replayTh: Thread
    val gameInitMs = System.currentTimeMillis()
    val gameCurMs: Long
        get() = System.currentTimeMillis() - gameInitMs

    init {
        controllers = Array(bots.size) { i -> FutureController(i.toLong(), requestsQ) }
        for ((i, c) in controllers.withIndex()) {
            val tank = Tank.byInitPos(nextUid, DPos2.ORIGIN)
            val tankPos = DPos2(4.5, 1.5)
            (tank.colPoly as ColMultiPart).baseColPoly.rotationCenter = tankPos
//            (tank.colPoly as ColMultiPart).baseColPoly.rotateAssignDeg(20.0)
            val tpanel = CoordPanel(IDim2(1, 1), IDim2(50, 50))
            tpanel.drawCollidable(tank.colPoly)
            tpanel.showFrame()
            map.addEle(tank)
            eventHistory.add(ElementCreateEvent(tank, gameCurMs))
            cidToTank[c.cid] = tank as Tank
        }

        val panel = map.quadTree.getCoordPanel(IDim2(1000, 1000))
        panel.showFrame()

        for ((i, bot) in bots.withIndex()) {
            botThs[i] =
                Thread {
                    bot.loop(controllers[i])
                }
            botThs[i]!!.start()
        }
        replayTh = Thread {
            replaySaver()
        }
        replayFile.appendText("[\n")
        replayTh.start()


        Runtime.getRuntime().addShutdownHook(Thread {
            stop()
        })
    }

    fun tankWeaponInfoHandler(req: ControllerRequest<Any>): Any {
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
                return cidToTank[req.cid]!!.tankRectBox
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
                return cidToTank[req.cid]!!.weapon.curCapacity
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

    fun replaySaver() {
        // read from eventHistory, save to file
        try {
            while (true) {
                Thread.sleep(100)
                while(eventHistory.isNotEmpty()) {
                    val curEvent = eventHistory.poll()
                    replayFile.appendBytes(curEvent.serialized + ",\n".toByteArray(Charsets.UTF_8))
//                    println("saved event: ${curEvent.serialized.toString(Charsets.UTF_8)}")
//                    println("cur file size: ${replayFile.length()}")
                }
            }
        } catch (e: InterruptedException) {
            return
        }
    }

    fun handleOtherRequests(req: ControllerRequest<Any>) {
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
        var lastUpd = System.currentTimeMillis()
        while (true) {
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
//            val dt = System.currentTimeMillis() - lastUpd
            val dt = 1L
            lastUpd = System.currentTimeMillis()
            for (updatable in map.timeUpdatables) {
                if (updatable is MovableElement && updatable.willMove(dt)) {
                    val colPolyAfterMove = updatable.colPolyAfterMove(dt)
                    val collideds = map.quadTree.collidedObjs(colPolyAfterMove)
                    for (collided in collideds) {
                        updatable.processCollision(map.collidableToEle[collided]!!)
                        map.collidableToEle[collided]!!.processCollision(updatable)
                    }

                    if (collideds.isNotEmpty()) {
                        println("collision detected: ")
                        // restore to the original position
                        continue
                    }

                    val prevPos = updatable.colPoly.rotationCenter
                    val prevAng = updatable.colPoly.angleRotated
                    val curPos = colPolyAfterMove.rotationCenter
                    val curAng = colPolyAfterMove.angleRotated
                    if (collideds.isEmpty() && (prevPos != curPos || prevAng != curAng)){
                        map.quadTree.remove(updatable.colPoly);
                        updatable.updateByTime(dt)
                        map.quadTree.insert(updatable.colPoly)

                        eventHistory.add(
                            ElementUpdateEvent(
                                updatable,
                                UpdateEventMask.defaultFalse(
                                    x = (prevPos.x errNE curPos.x),
                                    y = (prevPos.y errNE curPos.y),
                                    rad = (prevAng errNE curAng
                                            )
                                ), gameCurMs
                            )
                        )
                    }



                } else {
                    updatable.updateByTime(dt)
                }
            }
        }
    }


    fun stop() {
        // interrupt all the bots
        print("closing bot threads...")
        for (botTh in botThs) {
            botTh!!.interrupt()
        }
        println("done")
        // interrupt the replay saver
        print("closing replay saver thread...")
        replayTh!!.interrupt()
        println("done")
        // write the ending ] and close the replay file
        println("saving replay file...")
        replayFile.appendText("]")
        println("replay file saved")

    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            var curTime = LocalDate.now().toString() + "_" + LocalTime.now().toString()
            // create a new file of this name
            // replace all : with -
            curTime = curTime.replace(':', '-')
            val replayFile = File("./replayFiles/replay_@$curTime.json")

            println(Paths.get(replayFile.path.toString()).toAbsolutePath())
            replayFile.createNewFile()
            val game = Game(replayFile, RandomMovingBot())
            game.start()
        }
    }
}
typealias FutureBot = GameBot<FutureController>