package org.prismsus.tank.event

import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.*
import org.prismsus.tank.markets.AuctionProcessor
import org.prismsus.tank.markets.UpgradeRecord
import org.prismsus.tank.utils.game


abstract class MarketEvents(timeStamp: Long = game!!.elapsedGameMs) : GameEvent(timeStamp) {
    override val serialName: String = "MktUpd"
}

class AuctionUpdateEventBegin(
    val toSell: UpgradeRecord<out Number>,
    val endTime: Long,
    val minBid: Int = 1,
    timeStamp: Long = game!!.elapsedGameMs
) : MarketEvents(timeStamp) {
    override val serializedBytes: ByteArray

    init {
        val json = buildJsonObject {
            put("type", serialName)
            put("t", timeStamp)
            put("toSell", buildJsonArray {
                add(toSell.type.serialName)
                add(toSell.isInc)
                add(toSell.value)
            }
            )
            put("minBid", minBid)
            put("endT", endTime)
        }
        serializedBytes = json.toString().toByteArray()
    }
}


class AuctionUpdateEventMid(
    val bidRecord: AuctionProcessor.BidRecord,
    timeStamp: Long = game!!.elapsedGameMs
) : MarketEvents(timeStamp) {
    override val serializedBytes: ByteArray

    init {
        val json = buildJsonObject {
            put("type", serialName)
            put("t", timeStamp)
            put("bidder", game!!.cidToTank[bidRecord.cid]!!.playerName)
            put("price", bidRecord.price)
        }
        serializedBytes = json.toString().toByteArray()
    }
}

class AuctionUpdateEventEnd(
    val winningBidRecord: AuctionProcessor.BidRecord,
    val nextTime: Long,
    timeStamp: Long = game!!.elapsedGameMs
) : MarketEvents(timeStamp) {
    override val serializedBytes: ByteArray

    init {
        val json = buildJsonObject {
            put("type", serialName)
            put("t", timeStamp)
            put("buyer", game!!.cidToTank[winningBidRecord.cid]!!.playerName)
            put("price", winningBidRecord.price)
            put("nextT", nextTime)
        }
        serializedBytes = json.toString().toByteArray()
    }
}