package org.prismsus.tank.game

import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import org.prismsus.tank.bot.*
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.elements.GameMap
import org.prismsus.tank.elements.MovableElement
import org.prismsus.tank.elements.Tank
import org.prismsus.tank.event.*
import org.prismsus.tank.game.OtherRequests.*
import org.prismsus.tank.game.TankWeaponInfo.*
import org.prismsus.tank.markets.AuctionUserInterface
import org.prismsus.tank.markets.MarketImpl
import org.prismsus.tank.networkings.GuiCommunicator
import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.nextUid
import org.prismsus.tank.utils.collidable.ColMultiPart
import org.prismsus.tank.utils.collidable.DPos2
import java.io.File
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.PriorityBlockingQueue
import javax.swing.JFrame
import javax.swing.WindowConstants
import kotlin.math.PI

/**
 * @property replayFile The file to write game replay. If null, the game will not save game replay.
 */
class Game(val map: GameMap, vararg val bots: GameBot, debug: Boolean = false, val replayFile: File?) {
    val humanPlayerBots: Array<HumanPlayerBot> = bots.filterIsInstance<HumanPlayerBot>().toTypedArray()
    var controllers: Array<FutureController>
    val requestsQ = PriorityBlockingQueue<ControllerRequest<Any>>()
    val cidToTank = mutableMapOf<Long, Tank?>()
    val tankToCid = mutableMapOf<Tank, Long>()
    val lastCollidedEle = mutableMapOf<GameElement, ArrayList<GameElement>>()
    val botThs: Array<Thread?> = Array(bots.size) { null }
    val tankKilledOrder : ArrayList<Long> = ArrayList()
    private val replaySaver: ReplaySaver?
    private val debugTh: Thread?
    @Volatile
    var running : Boolean = false

    @Volatile
    var gameInitMs: Long = 0
    val elapsedGameMs: Long
        @Synchronized
        get() = System.currentTimeMillis() - gameInitMs
    var lastGameLoopMs = elapsedGameMs
    var marketImpl: MarketImpl = defAuction

    init {
        if (replayFile != null) {
            replaySaver = ReplaySaver(this, replayFile)
            replaySaver.save(INIT_EVENT)   // save init event to replay if possible
        } else {
            replaySaver = null
        }
        controllers =
            Array(bots.size) { i -> FutureController(i.toLong(), requestsQ, AuctionUserInterface(i.toLong())) }
        processNewEvent(MapCreateEvent(map, 0))
        for ((i, c) in controllers.withIndex()) {
            val tank = Tank.byInitPos(nextUid, DPos2.ORIGIN, bots[i].name)
            val tankPos = map.getUnoccupiedRandPos(tank.colPoly)!!
            (tank.colPoly as ColMultiPart).baseColPoly.rotationCenter = tankPos
//            val tPanel = CoordPanel(IDim2(1, 1), IDim2(50, 50))
//            tPanel.drawCollidable(tank.colPoly)
//            tPanel.showFrame()
            if ( bots[i] !is HumanPlayerBot
                ||(bots[i] is HumanPlayerBot && !(bots[i] as HumanPlayerBot).isObserver)) {
                map.addEle(tank)
                processNewEvent(ElementCreateEvent(tank, 0))
                cidToTank[c.cid] = tank
                tankToCid[tank] = c.cid
                game = this
                marketImpl.addPlayer(c.cid, c)
            }
        }

        if (debug) {
            debugTh = Thread {
                val frame = JFrame("collision box")
                frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
                frame.setSize(1000, 800)
                var panel = map.quadTree.getCoordPanel(IDim2(frame.size.width, frame.size.height))
                frame.contentPane.add(panel)
                frame.isVisible = true
                while (!Thread.interrupted()) {
                    frame.contentPane.remove(panel)
                    panel = map.quadTree.getCoordPanel(IDim2(frame.size.width, frame.size.height))
                    frame.contentPane.add(panel)
                    frame.revalidate()
                    frame.repaint()
                    Thread.sleep(DEF_DEBUG_MS_PER_LOOP)
                }
            }
            debugTh.start()
        } else {
            debugTh = null
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            stop()
        })
    }

    private fun tankWeaponInfoHandler(req: ControllerRequest<Any>): Any {
        val tk = cidToTank[req.cid]!!
        when (req.requestType) {
            TANK_HP -> {
                return tk.hp
            }

            TANK_MAX_HP -> {
                // TODO: add max hp field in tank
                return INIT_TANK_HP
            }

            TANK_LTRACK_SPEED -> {
                return tk.leftTrackVelo
            }

            TANK_RTRACK_SPEED -> {
                return tk.rightTrackVelo
            }

            TANK_TRACK_MAX_SPEED -> {
                return tk.trackMaxSpeed
            }

            TANK_COLBOX -> {
                return tk.tankRectBox.copy()
            }

            TANK_POS -> {
                return tk.colPoly.rotationCenter
            }

            TANK_ANGLE -> {
                // add because when angle=0, the tank is facing up
                return (tk.colPoly.angleRotated + PI / 2) % (2 * PI)
            }

            TANK_VIS_RANGE -> {
                return tk.visibleRange
            }

            WEAPON_RELOAD_RATE_PER_SEC -> {
                return tk.weapon.reloadRate
            }

            WEAPON_MAX_CAPACITY -> {
                return tk.weapon.maxCapacity
            }

            WEAPON_CUR_CAPACITY -> {
                return tk.weapon.curCapacity
            }

            WEAPON_DAMAGE -> {
                return tk.weapon.damage
            }

            WEAPON_COLBOX -> {
                return tk.weapon.colPoly
            }

            COMBINED_COLBOX -> {
                return tk.colPoly
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

    private fun handleOtherRequests(req: ControllerRequest<Any>) {
        val tk = cidToTank[req.cid]!!
        when (req.requestType) {
            GET_VISIBLE_ELEMENTS -> {
                // TODO(use quadtree to implement this)
                req.returnTo!!.complete(
                    ArrayList(map.gameEles).filter {
                        val dis = it.colPoly.rotationCenter.dis(tk.colPoly.rotationCenter)
                        dis <= tk.visibleRange && it != tk
                    }.deepCopyByKyro()
                )
            }

            GET_VISIBLE_TANKS -> {
                req.returnTo!!.complete(
                    ArrayList(map.tanks).filter {
                        val dis = it.colPoly.rotationCenter.dis(tk.colPoly.rotationCenter)
                        dis <= tk.visibleRange && it != tk
                    }.deepCopyByKyro()
                )
            }

            GET_VISIBLE_BULLETS -> {
                req.returnTo!!.complete(
                    ArrayList(map.bullets).filter {
                        val dis = it.colPoly.rotationCenter.dis(tk.colPoly.rotationCenter)
                        dis <= tk.visibleRange
                    }.deepCopyByKyro()
                )
            }

            CHECK_BLOCK_AT -> {
                val pos = req.params!!.first() as IPos2
                if (tk.colPoly.rotationCenter.dis(pos.toDPos2()) > tk.visibleRange) {
                    req.returnTo!!.complete(null)
                    return
                }
                val ret = map.blocks[pos.x][pos.y]
                req.returnTo!!.complete(ret.deepCopyByKyro())
            }

            CHECK_COLLIDING_GAME_ELES -> {
                val ret = lastCollidedEle[tk]?.filter {
                    val dis = it.colPoly.rotationCenter.dis(tk.colPoly.rotationCenter)
                    dis <= tk.visibleRange
                }
                req.returnTo!!.complete(ret.deepCopyByKyro())
            }

            GET_VISITED_ELEMENTS -> {
                // TODO: implement this
                req.returnTo!!.complete(ArrayList<GameElement>())
            }

            FIRE -> {

                val tankWeaponDirAng = tk.colPoly.angleRotated + PI / 2
                val bullet = tk.weapon.fire(tankWeaponDirAng)

                if (bullet != null) {
                    if (map.quadTree.allSubCols.contains(bullet.colPoly))
                        assert(false)
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

    private fun processNewEvent(evt: GameEvent) {
        if(replayFile != null) {
            replaySaver!!.save(evt)
        }
        for (hbot in humanPlayerBots) {
            hbot.evtsToClnt.add(evt)
        }
    }

    private fun handleRequests() {
        while (!requestsQ.isEmpty()) {
            val curReq = requestsQ.poll()
            if (curReq.cid !in cidToTank)
                continue
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

    private fun handleUpdatableElements(): ArrayList<GameElement> {
        val dt = elapsedGameMs - lastGameLoopMs
        val toRemove = ArrayList<GameElement>()
        for (updatable in map.timeUpdatables) {
            if (updatable is MovableElement && updatable.willMove(dt)) {
                if (updatable.colPoly is ColMultiPart)
                    (updatable.colPoly as ColMultiPart).checks()
                val colPolyAfterMove = updatable.colPolyAfterMove(dt)
                val collideds = map.quadTree.collidedObjs(colPolyAfterMove)
                collideds.remove(updatable.colPoly)
                val prevHp = updatable.hp
                var curHp = updatable.hp
                for (collided in collideds) {
                    val otherGe = map.collidableToEle[collided]!!
                    updatable.processCollision(otherGe)
                    lastCollidedEle.getOrPut(updatable) { ArrayList() }.add(otherGe)
                    lastCollidedEle.getOrPut(otherGe) { ArrayList() }.add(updatable)
                    curHp = updatable.hp
                    val hpChanged = otherGe.processCollision(updatable)
                    if (hpChanged) {
                        processNewEvent(
                            ElementUpdateEvent(
                                otherGe,
                                UpdateEventMask.defaultFalse(
                                    hp = true
                                ),
                                elapsedGameMs
                            )
                        )
                        if (otherGe.removeStat == GameElement.RemoveStat.TO_REMOVE) {
                            toRemove.add(otherGe)
                        }
                    }
                }

                if (updatable.removeStat == GameElement.RemoveStat.TO_REMOVE) {
                    toRemove.add(updatable)
                }

                if (collideds.isNotEmpty()) {
                    continue
                }

                val prevPos =
                    if (updatable.colPoly is ColMultiPart) (updatable.colPoly as ColMultiPart).baseColPoly.rotationCenter else updatable.colPoly.rotationCenter
                val prevAng = updatable.colPoly.angleRotated
                val curPos =
                    if (colPolyAfterMove is ColMultiPart) (colPolyAfterMove).baseColPoly.rotationCenter else colPolyAfterMove.rotationCenter
                val curAng = colPolyAfterMove.angleRotated
                if (collideds.isEmpty() && (prevPos != curPos || prevAng != curAng)) {
                    assert(map.quadTree.remove(updatable.colPoly))
                    updatable.colPoly.becomeNonCopy(colPolyAfterMove)
                    map.quadTree.insert(updatable.colPoly)
                    processNewEvent(
                        ElementUpdateEvent(
                            updatable,
                            UpdateEventMask.defaultFalse(
                                x = (prevPos.x errNE curPos.x),
                                y = (prevPos.y errNE curPos.y),
                                rad = (prevAng errNE curAng),
                                hp = (prevHp != curHp)
                            ), elapsedGameMs
                        )
                    )
                }
            } else {
                updatable.updateByTime(dt)
            }
        }
        for (entry in lastCollidedEle) {
            lastCollidedEle[entry.key] = ArrayList(entry.value.distinct())
        }
        return ArrayList(toRemove.distinct())
    }

    fun start() {
        gameInitMs = System.currentTimeMillis()
        lastGameLoopMs = elapsedGameMs
        marketImpl.start()

        for ((i, bot) in bots.withIndex()) {
            botThs[i] =
                Thread {
                    try {
                        if (bot.isFutureController)
                            bot.loop(controllers[i])
                        else
                            bot.loop(Controller(controllers[i]))
                    } catch (e: InterruptedException) {
                        println("Bot ${bot.name} interrupted")
                    }
                }
            botThs[i]!!.start()
        }


        running = true
        while (true) {
            // first handle all the requests, then move all the elements
            val loopStartMs = elapsedGameMs
            handleRequests()
            val toRemove = handleUpdatableElements()
            for (rem in toRemove) {
                map.remEle(rem)
                if (rem is Tank) {
                    val cid = tankToCid[rem]!!
                    val bot = bots[cid.toInt()]
                    cidToTank.remove(cid)
                    tankToCid.remove(rem)
                    if (bot !is HumanPlayerBot)
                        botThs[cid.toInt()]!!.interrupt()
                    tankKilledOrder.add(rem.uid)
                }
                processNewEvent(ElementRemoveEvent(rem.uid, elapsedGameMs))
                if (map.tanks.size == 1){
                    // game end here
                    val rankMap = mutableMapOf<Long, Long>()
                    rankMap[map.tanks[0].uid] = 1
                    for ((rk, uid) in tankKilledOrder.reversed().withIndex()){
                        rankMap[uid] = rk.toLong() + 2
                    }
                    processNewEvent(GameEndEvent(rankMap))
                    stop()
                }
            }

            marketImpl.toBeUpgrade.removeIf{
                val tk = cidToTank[it.cid] ?: return@removeIf true
                val evt = tk.processUpgrade(it)
                processNewEvent(evt)
                true
            }

            marketImpl.evtToBeSent.removeIf{
                processNewEvent(it)
                true
            }

            val loopEndMs = elapsedGameMs
            val loopLen = loopEndMs - loopStartMs
//            println("cur loop len: $loopLen, slept for ${DEF_MS_PER_LOOP - loopLen}")
            lastGameLoopMs = elapsedGameMs
            if (loopLen < DEF_MS_PER_LOOP)
                Thread.sleep(DEF_MS_PER_LOOP - loopLen)
        }
    }


    fun stop() {
        if(!running) {
            return
        }
        running = false
        // close websocket connection
        runBlocking {
            if (humanPlayerBots.isNotEmpty()) {
                print("closing websockets...")
                for (bot in humanPlayerBots) {
                    bot.webSockSession.close(reason = CloseReason(CloseReason.Codes.NORMAL, "Game ended"))
                }
                println("done")
            }
        }
        // interrupt all the bots
        print("closing bot threads...")
        for (botTh in botThs) {
            botTh!!.interrupt()
            botTh.stop()
        }
        println("done")
        // interrupt debug thread
        if (debugTh != null) {
            print("closing debug thread...")
            debugTh.interrupt()
            println("done")
        }
        if (replaySaver != null) {
            print("saving replay file...")
            replaySaver.stop()
            replaySaver.postProcess()
            println("done")
        }
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
            val communicator = GuiCommunicator(1)
            communicator.start()
            val players = communicator.humanPlayerBots.get()
            val randBots = Array(1) { RandomMovingBot() }
            val aimingBots = Array(1) { TankAimingBot() }
            val auctBots = Array(1) { AuctTestBot() }
            val game = Game(
                GameMap("15x15.json"), *aimingBots, *randBots, *players.toTypedArray(), *auctBots,
                debug = false, replayFile = replayFile
            )
            game.start()
        }
    }
}