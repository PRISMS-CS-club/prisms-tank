package org.prismsus.tank.markets

import org.prismsus.tank.bot.FutureController
import org.prismsus.tank.event.MarketEvents
import org.prismsus.tank.utils.CompNum
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class None : MarketImpl{
    override val toBeUpgrade: BlockingQueue<UpgradeRecord<out Number>>
        get() = LinkedBlockingQueue()
    override val upgradableItemsInfo: List<UpgradeEntry<out CompNum>>
        get() = emptyList()
    override val evtToBeSent: BlockingQueue<MarketEvents>
        get() = LinkedBlockingQueue()
    override val playerCids: HashSet<Long>
        get() = HashSet()
    override val type: MarketType
        get() = MarketType.NONE

    override fun start() {
    }
    override fun addPlayer(cid: Long, controller: FutureController) {
    }

    override fun stop() {
    }

    override fun processGUIevts(cid: Long, type: String, params: Array<*>) {
    }

}