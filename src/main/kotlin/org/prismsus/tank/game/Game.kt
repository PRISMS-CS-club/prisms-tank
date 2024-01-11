package org.prismsus.tank.game

import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import org.prismsus.tank.bot.*
import org.prismsus.tank.elements.*
import org.prismsus.tank.event.*
import org.prismsus.tank.game.OtherRequests.*
import org.prismsus.tank.game.TankWeaponInfo.*
import org.prismsus.tank.markets.AuctionUserInterface
import org.prismsus.tank.markets.MarketImpl
import org.prismsus.tank.networkings.GuiCommunicator
import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.ColMultiPart
import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.collidable.DPos2
import java.io.File
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
import javax.swing.JFrame
import javax.swing.WindowConstants
import kotlin.math.PI

/**
 * @property replayFile The file to write game replay. If null, the game will not save game replay.
 */
class Game(val map: GameMap, vararg val bots: GameBot, val debug: Boolean = false, val replayFile: File?) {
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
        if (evt is DebugEvent && evt.debugType.severity > DebugEvent.storeIfAboveOrEqual.severity)
            return
        if(replayFile != null) {
            replaySaver!!.save(evt)
        }
        for (hbot in humanPlayerBots) {
            hbot.evtsToClnt.add(evt)
        }
    }

    fun dbgMessage(msg: String, severity : DebugEvent.DebugType = DebugEvent.printIfAboveOrEqual) {
        processNewEvent(DebugEvent(msg, severity))
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
        val colsAfterMove = mutableSetOf<ColPoly>()
        val afterMoveToBeforeMove = mutableMapOf<ColPoly, ColPoly>()
        val invalidlyMovedColPolys = ArrayList<ColPoly>() // after movement, some colpoly will collide with others

        for (updatable in map.timeUpdatables) {
            if (updatable is MovableElement && updatable.willMove(dt)) {
                if (!map.quadTree.allSubCols.contains(updatable.colPoly))
                    dbgMessage("colpoly not in quadtree")
                map.quadTree.remove(updatable.colPoly)
            }
            else {
                updatable.updateByTime(dt)
            }
        }

        for (ele in map.movables){
            if (!ele.willMove(dt)) continue
            val colAfterMove = ele.colPolyAfterMove(dt)
            colsAfterMove.add(colAfterMove)
            afterMoveToBeforeMove[colAfterMove] = ele.colPoly
            map.quadTree.insert(colAfterMove)
        }


        for (movedColPoly in colsAfterMove){
            val collideds = map.quadTree.collidedObjs(movedColPoly)
            collideds.remove(movedColPoly)
            val colBeforeMove1 = afterMoveToBeforeMove[movedColPoly]!!
            val colGe1 = map.collidableToEle[colBeforeMove1]!! as MovableElement
            for (col in collideds){
                // here all collideds are colpoly after movement
                var colGe2 = map.collidableToEle[col]
                if (col in colsAfterMove){
                    val colBeforeMove2 = afterMoveToBeforeMove[col]!!
                    colGe2 = map.collidableToEle[colBeforeMove2]!! as MovableElement
                }
                var hpChanged =  colGe1.processCollision(colGe2!!)
                if (hpChanged){
                    processNewEvent(
                        ElementUpdateEvent(
                            colGe1,
                            UpdateEventMask.defaultFalse(
                                hp = true
                            ),
                        )
                    )
                }
                hpChanged = colGe2.processCollision(colGe1)
                if (hpChanged){
                    processNewEvent(
                        ElementUpdateEvent(
                            colGe2,
                            UpdateEventMask.defaultFalse(
                                hp = true
                            ),
                        )
                    )
                }

                if (colGe1.removeStat == GameElement.RemoveStat.TO_REMOVE){
                    toRemove.add(colGe1)
                }
                if (colGe2.removeStat == GameElement.RemoveStat.TO_REMOVE){
                    toRemove.add(colGe2)
                }
                // TODO: maintain lastCollidedEle
//                (lastCollidedEle[colGe1] ?: lastCollidedEle.put(colGe1, ArrayList())!!).add(colGe2)
            }

            if (collideds.isEmpty()){
                // the movement is valid
                val prevAng = colGe1.colPoly.angleRotated
                val prevPos = colGe1.colPoly.rotationCenter.copy()
                colGe1.updateByTime(dt)
                val curAng = colGe1.colPoly.angleRotated
                val curPos = colGe1.colPoly.rotationCenter.copy()
                processNewEvent(
                    ElementUpdateEvent(
                        colGe1,
                        UpdateEventMask.defaultFalse(
                            x = curPos.x != prevPos.x,
                            y = curPos.y != prevPos.y,
                            rad = curAng != prevAng
                        ),
                        elapsedGameMs
                    )
                )
            }

        }

        for (movedColPoly in colsAfterMove){
            map.quadTree.remove(movedColPoly)
            map.quadTree.insert(afterMoveToBeforeMove[movedColPoly]!!)
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
                debug = true, replayFile = replayFile
            )
            game.start()
        }
    }
}