package org.prismsus.tank.markets

import org.prismsus.tank.bot.FutureController
import org.prismsus.tank.event.MarketEvents
import org.prismsus.tank.utils.CompNum
import java.util.concurrent.BlockingQueue
enum class MarketType {
    AUCTION {
        override val serialName = "auction"
    };
    abstract val serialName : String
}
interface MarketUserInterface{
    val type : MarketType
    val upgradableItemsInfo : List<UpgradeEntry<out CompNum>>
}
interface MarketImpl : MarketUserInterface{
    val evtToBeSent : BlockingQueue<MarketEvents>
    val toBeUpgrade : BlockingQueue<UpgradeRecord<out Number>>
    val playerCids : HashSet<Long>
    fun start()
    fun stop()
    fun addPlayer(cid : Long, controller : FutureController)
    fun processGUIevts(cid : Long, type : String, params : Array<*>)
}
