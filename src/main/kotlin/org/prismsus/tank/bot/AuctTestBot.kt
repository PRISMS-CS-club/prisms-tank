package org.prismsus.tank.bot

class AuctTestBot : GameBot{
    override fun getName(): String {
        return "AuctTestBot"
    }

    override fun loop(c: FutureController?) {
        val m = c!!.market
        val MAX_BID = 10;
        while(!Thread.interrupted()) {
            if (m.inAuction){
                if (m.curWinningBid!!.price < MAX_BID) {
                    m.bid(m.curWinningBid!!.price + 1)
                    println("test bot is bidding ${m.curWinningBid!!.price + 1}")
                }
            }
            Thread.sleep(50)
        }
    }
    override fun isFutureController(): Boolean {
        return true
    }
}