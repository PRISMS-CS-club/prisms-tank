package org.prismsus.tank.game

import org.prismsus.tank.bot.*
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.elements.GameMap
import org.prismsus.tank.elements.MovableElement
import org.prismsus.tank.elements.Tank
import org.prismsus.tank.event.*
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

class Game(val replayFile: File, vararg val bots: GameBot) {
    val humanPlayerBots : Array<HumanPlayerBot> = bots.filterIsInstance<HumanPlayerBot>().toTypedArray()
    val eventHistoryToSave = PriorityBlockingQueue<GameEvent>()
    var controllers: Array<FutureController>
    val requestsQ = PriorityBlockingQueue<ControllerRequest<Any>>()
    val map = GameMap("default.json")
    val cidToTank = mutableMapOf<Long, Tank>()
    val botThs: Array<Thread?> = Array(bots.size) { null }
    lateinit var replayTh: Thread
    val gameInitMs = System.currentTimeMillis()
    val elapsedGameMs: Long
        get() = System.currentTimeMillis() - gameInitMs
    var lastGameLoopMs = elapsedGameMs
    init {
        controllers = Array(bots.size) { i -> FutureController(i.toLong(), requestsQ) }
        for ((i, c) in controllers.withIndex()) {
            val tank = Tank.byInitPos(nextUid, DPos2.ORIGIN, bots[i].name)
            val tankPos = DPos2(4.5, 1.5)
            (tank.colPoly as ColMultiPart).baseColPoly.rotationCenter = tankPos
            val tpanel = CoordPanel(IDim2(1, 1), IDim2(50, 50))
            tpanel.drawCollidable(tank.colPoly)
            tpanel.showFrame()
            tpanel.showFrame()
            map.addEle(tank)
            eventHistoryToSave.add(ElementCreateEvent(tank, elapsedGameMs))
            cidToTank[c.cid] = tank
        }

        val panel = map.quadTree.getCoordPanel(IDim2(1000, 1000))
        panel.showFrame()

        for ((i, bot) in bots.withIndex()) {
            botThs[i] =
                Thread {
                    if (bot.isUseFutureController)
                        bot.loop(controllers[i])
                    else
                        bot.loop(Controller(controllers[i]))
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
        for (hbot in humanPlayerBots){
            hbot.evtsToClnt.add(MapCreateEvent(map))
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
                while(eventHistoryToSave.isNotEmpty()) {
                    val curEvent = eventHistoryToSave.poll()
                    replayFile.appendBytes(curEvent.serializedBytes + ",\n".toByteArray(Charsets.UTF_8))
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
                if (bullet != null) {
                    map.addEle(bullet)
                    processNewEvent(ElementCreateEvent(bullet, elapsedGameMs))
                }
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

    fun processNewEvent(evt : GameEvent){
        eventHistoryToSave.add(evt)
        for (hbot in humanPlayerBots){
            hbot.evtsToClnt.add(evt)
        }
    }
    fun handleRequests(){
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

    fun handleUpdatableElements() : ArrayList<GameElement>{
        val dt = elapsedGameMs - lastGameLoopMs
        println("dt = $dt")
        val toRem = ArrayList<GameElement>()
        for (updatable in map.timeUpdatables) {
            if (updatable is MovableElement && updatable.willMove(dt)) {
                if (updatable.colPoly is ColMultiPart)
                    (updatable.colPoly as ColMultiPart).checks()
                val colPolyAfterMove = updatable.colPolyAfterMove(dt)
                val collideds = map.quadTree.collidedObjs(colPolyAfterMove)
                collideds.remove(updatable.colPoly)
                for (collided in collideds) {
                    updatable.processCollision(map.collidableToEle[collided]!!)
                    map.collidableToEle[collided]!!.processCollision(updatable)
                }

                if (updatable.removeStat == GameElement.RemoveStat.TO_REMOVE) {
                    toRem.add(updatable)
                }

                if (collideds.isNotEmpty()) {
                    println("collision detected: ")
                    continue
                }

                val prevPos = if (updatable.colPoly is ColMultiPart) (updatable.colPoly as ColMultiPart).baseColPoly.rotationCenter else  updatable.colPoly.rotationCenter
                val prevAng = updatable.colPoly.angleRotated
                val curPos = if (colPolyAfterMove is ColMultiPart) (colPolyAfterMove).baseColPoly.rotationCenter else  colPolyAfterMove.rotationCenter
                val curAng = colPolyAfterMove.angleRotated
                if (collideds.isEmpty() && (prevPos != curPos || prevAng != curAng)){
                    map.quadTree.remove(updatable.colPoly);
                    updatable.colPoly.becomeNonCopy(colPolyAfterMove)
                    map.quadTree.insert(updatable.colPoly)
//                        println("cur ang: ${updatable.colPoly.angleRotated}")
                    processNewEvent(
                        ElementUpdateEvent(
                            updatable,
                            UpdateEventMask.defaultFalse(
                                x = (prevPos.x errNE curPos.x),
                                y = (prevPos.y errNE curPos.y),
                                rad = (prevAng errNE curAng
                                        )
                            ), elapsedGameMs
                        )
                    )
                }
            } else {
                updatable.updateByTime(dt)
            }
        }
        return toRem
    }

    fun start() {
        lastGameLoopMs = elapsedGameMs
        while (true) {
            // first handle all the requests, then move all the elements
            val loopStartMs = elapsedGameMs
            handleRequests()
            val toRem = handleUpdatableElements()
            for (rem in toRem) {
                map.remEle(rem)
                processNewEvent(ElementRemoveEvent(rem.uid, elapsedGameMs))
            }
            val loopEndMs = elapsedGameMs
            val loopLen = loopEndMs - loopStartMs
            println("cur loop len: $loopLen, slept for ${DEF_MS_PER_LOOP - loopLen}")
            lastGameLoopMs = elapsedGameMs
            if (loopLen < DEF_MS_PER_LOOP)
                Thread.sleep(DEF_MS_PER_LOOP - loopLen)
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