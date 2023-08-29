package org.prismsus.tank.event

import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.*
import org.prismsus.tank.markets.AuctionProcessor
import org.prismsus.tank.markets.UpgradeRecord
import org.prismsus.tank.utils.game
import org.prismsus.tank.utils.toEvtFixed


abstract class MarketEvents(timeStamp: Long = game!!.elapsedGameMs) : GameEvent(timeStamp) {
    override val serialName: String = "MktUpd"
}

class AuctionUpdateEventBegin(
    val toSell: UpgradeRecord<out Number>,
    val endTime: Long,
    val minBid: Int = 1,
    timeStamp: Long = game!!.elapsedGameMs
) : MarketEvents(timeStamp) {

    init {
        val tmp = buildMap {
            put("toSell", buildJsonArray {
                add(toSell.type.serialName)
                add(toSell.isInc)
                add(toSell.value.toEvtFixed())
            }
            )
            put("minBid", minBid)
            put("endT", endTime)
        }
        mp.putAll(tmp)
    }
}


class AuctionUpdateEventMid(
    val bidRecord: AuctionProcessor.BidRecord,
    timeStamp: Long = game!!.elapsedGameMs
) : MarketEvents(timeStamp) {

    init {
        val tmp = buildMap {
            put("bidder", game!!.cidToTank[bidRecord.cid]!!.uid)
            put("price", bidRecord.price)
        }
        mp.putAll(tmp)
    }
}

class AuctionUpdateEventEnd(
    val winningBidRecord: AuctionProcessor.BidRecord,
    val nextTime: Long,
    timeStamp: Long = game!!.elapsedGameMs
) : MarketEvents(timeStamp) {
    init {
        val tmp = buildMap {
            game!!.cidToTank[winningBidRecord.cid]?.let {
                put("buyer", it.uid)
                put("price", winningBidRecord.price)
            }
            put("nextT", nextTime)
        }
        mp.putAll(tmp)
    }
}