package org.prismsus.tank.utils

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import org.objenesis.strategy.StdInstantiatorStrategy
import org.prismsus.tank.elements.GameMap
import org.prismsus.tank.game.Game
import org.prismsus.tank.markets.AuctionProcessor

/*
* shared variables
* */

@Volatile
var nextUid : Long = 0
    @Synchronized get() = field++
    private set

@Volatile
var gameMap : GameMap? = null

@Volatile
var game : Game? = null

@Volatile
var defAuction = AuctionProcessor(
    DEF_AUC_DURATION_MS,
    DEF_AUC_INTERV_MS,
    DEF_AUC_BID_INTERV
)

val thSafeKyro = ThreadLocal.withInitial({ Kryo() })
    get(){
        field.get().isRegistrationRequired = false
        field.get().instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())
        return field
    }