package org.prismsus.tank.markets

import org.prismsus.tank.bot.FutureController
import org.prismsus.tank.event.AuctionUpdateEventBegin
import org.prismsus.tank.event.AuctionUpdateEventEnd
import org.prismsus.tank.event.AuctionUpdateEventMid
import org.prismsus.tank.event.MarketEvents
import org.prismsus.tank.utils.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class AuctionProcessor(
    val aucDuration: Long,
    val aucInterval: Long,
    val bidInterval: Long,
) : MarketImpl {
    override val type: MarketType = MarketType.AUCTION
    override val upgradableItemsInfo: List<UpgradeEntry<out CompNum>> = listOf(
        UpgradeEntry(
            UpgradeEntry.UpgradeType.MAX_HP,
            1.toComp()..50.toComp(),
            50.toComp()..300.toComp(),
            1.0
        ),
        UpgradeEntry(
            UpgradeEntry.UpgradeType.VIS_RADIUS,
            .25.toComp()..4.0.toComp(),
            4.5.toComp()..10.0.toComp(),
            1.0
        ),
//        UpgradeEntry(
//            UpgradeEntry.UpgradeType.TANK_BODY_AREA,
//            1,
//            1,
//            1,
//            1,
//            1.0
//        ),
//        UpgradeEntry(
//            UpgradeEntry.UpgradeType.TANK_BODY_EDGE_CNT,
//            1,
//            1,
//            1,
//            1,
//            1.0
//        ),
        UpgradeEntry(
            UpgradeEntry.UpgradeType.TANK_SPEED,
            .25.toComp()..4.0.toComp(),
            3.5.toComp()..15.0.toComp(),
            1.0
        ),
//        UpgradeEntry(
//            UpgradeEntry.UpgradeType.API_TOKEN_CNT,
//            1,
//            1,
//            1,
//            1,
//            1.0
//        ),
        UpgradeEntry(
            UpgradeEntry.UpgradeType.WEAPON_CAPACITY,
            5.toComp()..50.toComp(),
            25.toComp()..200.toComp(),
            1.0
        ),
        UpgradeEntry(
            UpgradeEntry.UpgradeType.WEAPON_DAMAGE,
            1.toComp()..20.toComp(),
            15.toComp()..50.toComp(),
            1.0
        ),
        UpgradeEntry(
            UpgradeEntry.UpgradeType.WEAPON_LAUNCH_MIN_INTERV,
            -50.toComp()..-5.toComp(),
            50.toComp()..295.toComp(),
            1.0
        ),
        UpgradeEntry(
            UpgradeEntry.UpgradeType.WEAPON_RELOAD_RATE,
            .25.toComp()..3.0.toComp(),
            3.5.toComp()..10.0.toComp(),
            1.0
        ),
        UpgradeEntry(
            UpgradeEntry.UpgradeType.WEAPON_BULLET_SPEED,
            1.0.toComp()..15.0.toComp(),
            8.5.toComp()..25.0.toComp(),
            1.0
        ),
    )


    data class BidRecord(val cid: Long, val price: Int, val timeStamp: Long)

    private fun handleNewAuct() {
        // get the probablity field in upgradableEntry
        val selDistrib = upgradableItemsInfo.map { it.probability }.toTypedArray()
        val isInc = Math.random() > 0.5
        val updEntry = upgradableItemsInfo.randomSelect(selDistrib)
        val value = if (isInc) updEntry.incrementRg.genRand() else updEntry.valRg.genRand()
        curAuctionItem = UpgradeRecord(updEntry.type, isInc, value, -1, game!!.elapsedGameMs)
        curWinningBid = BidRecord(-1, 0, game!!.elapsedGameMs)
        // turn off inAuction after aucDuration ms
        for ((cid, callback) in onAuctStartCallbacks) {
            controllers[cid]!!.createThread(callback)?.start()
        }
        auctionEndTime = game!!.elapsedGameMs + aucDuration
        evtToBeSent.add(AuctionUpdateEventBegin(curAuctionItem!!, auctionEndTime))
        inAuction = true
        Thread.sleep(aucDuration)
        inAuction = false
        lstAuctEndTime = game!!.elapsedGameMs
        evtToBeSent.add(AuctionUpdateEventEnd(curWinningBid!!, nextAuctionTime))
    }

    fun bid(cid: Long, price: Int): Boolean {
        assert(playerCids.contains(cid))
        if (!inAuction) return false
        synchronized(lstUserBidAttemptTime) {
            lstUserBidAttemptTime.putIfAbsent(cid, game!!.elapsedGameMs)
        }
        if (game!!.elapsedGameMs - lstUserBidAttemptTime[cid]!! < bidInterval) return false
        if (price <= curWinningBid!!.price) return false
        curWinningBid = BidRecord(cid, price, game!!.elapsedGameMs)
        bidHistroy!!.add(curWinningBid!!)
        evtToBeSent.add(AuctionUpdateEventMid(curWinningBid!!))
        return true
    }

    fun setOnAuctionStart(cid: Long, callback: () -> Unit): Boolean {
        assert(playerCids.contains(cid))
        val con = controllers[cid]!!
        if (con.threadCount >= FutureController.MAX_THREAD) return false
        synchronized(onAuctStartCallbacks) {
            onAuctStartCallbacks[cid] = callback
        }
        return true
    }


    override fun start() {
        running = true;
        Thread {
            while (running) {
                handleNewAuct()
                Thread.sleep(aucInterval)
            }
        }.start()
    }

    override fun stop() {
        running = false
    }

    override fun addPlayer(cid: Long, controller: FutureController) {
        assert(!playerCids.contains(cid))
        playerCids.add(cid)
        controllers[cid] = controller
    }

    override fun processGUIevts(cid : Long, type: String, params: Array<*>) {
        when (type) {
            "bid" -> {
                bid(cid, params[0] as Int)
            }
            else -> {
                throw Exception("unknown event type")
            }
        }
    }

    override val toBeUpgrade: BlockingQueue<UpgradeRecord<out Number>> = LinkedBlockingQueue()
    override val evtToBeSent: BlockingQueue<MarketEvents> = LinkedBlockingQueue()
    val nextAuctionTime: Long
        @Synchronized
        get() {
            if (inAuction) return -1
            return lstAuctEndTime + aucInterval
        }

    @Volatile
    var curAuctionItem: UpgradeRecord<out Number>? = null
        get() {
            if (!inAuction) return null
            return field
        }

    @Volatile
    var bidHistroy: ArrayList<BidRecord>? = null
        get() {
            if (!inAuction) return null
            return field
        }

    @Volatile
    var curWinningBid: BidRecord? = null
        get() {
            if (!inAuction) return null
            return field
        }

    @Volatile
    var inAuction: Boolean = false
        @Synchronized
        get() = field

    @Volatile
    var lstAuctEndTime: Long = -1
        @Synchronized
        get() = field

    @Volatile
    var auctionEndTime: Long = -1
        @Synchronized
        get() {
            if (!inAuction) return -1
            return field
        }

    @Volatile
    private var running = false

    val lstUserBidAttemptTime: MutableMap<Long, Long> = mutableMapOf()
    val onAuctStartCallbacks: MutableMap<Long, () -> Unit> = mutableMapOf()
    private val controllers: HashMap<Long, FutureController> = hashMapOf()
    override val playerCids: HashSet<Long> = hashSetOf()
}


class AuctionUserInterface(private val cid: Long) : MarketUserInterface by defAuction {
    fun bid(price: Int) = defAuction.bid(cid, price)
    fun setOnAuctionStart(callback: () -> Unit): Boolean = defAuction.setOnAuctionStart(cid, callback)

    val upgradableItemInfo : Array<UpgradeEntry<out CompNum>>
        get() = defAuction.upgradableItemsInfo.map {it.copy()}.toTypedArray()

    val nextAuctionTime: Long
        get() = defAuction.nextAuctionTime
    val lstAuctionEndTime: Long
        get() = defAuction.lstAuctEndTime
    val auctionEndTime: Long
        get() = defAuction.auctionEndTime
    val curAuctionItem: UpgradeRecord<out Number>?
        get() = defAuction.curAuctionItem?.copy()
    val bidHistroy: Array<AuctionProcessor.BidRecord>?
        get() =  defAuction.bidHistroy?.map { it.copy() }?.toTypedArray()
    val curWinningBid: AuctionProcessor.BidRecord?
        get() = defAuction.curWinningBid?.copy()
    val inAuction: Boolean
        get() = defAuction.inAuction
    val lstBidAttemptTime: Long
        get() = synchronized(defAuction.lstUserBidAttemptTime) {
            return defAuction.lstUserBidAttemptTime[cid]!!
        }
    val curOnAuctionStartCallback: (() -> Unit)?
        get() = synchronized(defAuction.onAuctStartCallbacks) {
            return defAuction.onAuctStartCallbacks[cid]
        }

}
