package org.prismsus.tank.game

import org.prismsus.tank.bot.FutureController
import org.prismsus.tank.bot.GameBot
import org.prismsus.tank.bot.RandomMovingBot
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.elements.GameMap
import org.prismsus.tank.elements.MovableElement
import org.prismsus.tank.elements.Tank
import org.prismsus.tank.event.ElementUpdateEvent
import org.prismsus.tank.event.GameEvent
import org.prismsus.tank.event.UpdateEventSlect
import java.util.concurrent.PriorityBlockingQueue
import org.prismsus.tank.game.TankWeaponInfo.*
import org.prismsus.tank.game.OtherRequests.*
import org.prismsus.tank.utils.*
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalTime

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
            val tankPos = map.getUnoccupiedRandPos(INIT_TANK_COLBOX)
            var uid = 0
            val tank = map.addEle(Tank.byInitPos(nextUid, tankPos))
            cidToTank[c.cid] = tank as Tank
        }

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
        var lastUpd = System.currentTimeMillis()


        Runtime.getRuntime().addShutdownHook(Thread {
            println("saving replay file...")
            replayFile.appendText("]")
            println("replay file saved")
            stop()
        })

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

            val dt = System.currentTimeMillis() - lastUpd
            lastUpd = System.currentTimeMillis()
            for (updatable in map.timeUpdatables) {
                if (updatable is MovableElement) {
                    val prevPos = updatable.colPoly.rotationCenter.copy()
                    val prevAng = updatable.colPoly.angleRotated
                    updatable.updateByTime(dt)
                    val curPos = updatable.colPoly.rotationCenter.copy()
                    val curAng = updatable.colPoly.angleRotated
                    val collideds = map.quadTree.collidedObjs(updatable.colPoly)
                    for (collided in collideds) {
                        updatable.processCollision(map.collidableToEle[collided]!!)
                        map.collidableToEle[collided]!!.processCollision(updatable)
                    }

                    if (collideds.isNotEmpty()) {
                        print("collision detected: ")
                        // restore to the original position
                        updatable.colPoly.rotationCenter = prevPos
                        updatable.colPoly.rotateTo(prevAng)
                        continue
                    }


                    if (collideds.isEmpty() && (prevPos != curPos || prevAng != curAng))
                        eventHistory.add(
                            ElementUpdateEvent(
                                updatable,
                                UpdateEventSlect.defaultFalse(
                                    x = (prevPos.x != curPos.x),
                                    y = (prevPos.y != curPos.y),
                                    rad = (prevAng != curAng
                                            )
                                ), gameCurMs
                            )
                        )


                } else {
                    updatable.updateByTime(dt)
                }
            }
        }
    }


    fun stop() {
        // interrupt all the bots
        for (botTh in botThs) {
            botTh!!.interrupt()
        }
        // interrupt the replay saver
        replayTh!!.interrupt()
        // write the ending ] and close the replay file
        replayFile.appendText("]")
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