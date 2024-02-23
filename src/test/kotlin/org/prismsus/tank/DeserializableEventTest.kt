package org.prismsus.tank

import org.junit.jupiter.api.Test
import org.prismsus.tank.event.BotInitEvent
import org.prismsus.tank.event.BotRequestEvent
import org.prismsus.tank.event.ServerResponseEvent
import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.collidable.DPos2

class DeserializableEventTest {


    fun genRandomColPoly(ptsCnt : Int) : ColPoly{
        val pts = mutableListOf<DPos2>()
        for(i in 0 until ptsCnt){
            pts.add(DPos2(Math.random(), Math.random()))
        }
        return ColPoly(pts.toTypedArray())
    }

    @Test
    fun testKyroSerializationAndDeserialization(){
        val colPoly = genRandomColPoly(5)
        val serialized = colPoly.serializeByKyro()
        val colPoly2 = serialized.deserializeByKyro() as ColPoly
        assert(colPoly == colPoly2)

    }

    @Test
    fun testBinSerializationForJson(){
        val colPoly = genRandomColPoly(5)
        val serialized = colPoly.binSerializationToSendThroughJson()
        val colPoly2 = serialized.binDeserializationFromJson() as ColPoly
        assert(colPoly == colPoly2)
    }

    @Test
    fun testServerResponseEvent(){
        val REP_TIMES = 1000
        val curTime = System.currentTimeMillis()
        for (i in 0 until REP_TIMES) {
            val colPoly = genRandomColPoly(10)
            val evt = ServerResponseEvent(colPoly, 1145, 1919810, -1)
            val serializedStr = evt.serializedStr
            val evt2 = evt.deserialize(serializedStr) as ServerResponseEvent
            assert(evt2.timeStamp == evt.timeStamp)
            assert(evt2.serialName == evt.serialName)
            assert(evt2.returnValue == evt.returnValue)
        }
        val elapsed = System.currentTimeMillis() - curTime
        val aveTime = elapsed.toFloat() / REP_TIMES
        println("average time for $REP_TIMES times of serialization and deserialization: $aveTime ms")
    }

    @Test
    fun testBotInitEvent(){
        val evt = BotInitEvent("testBot", 114514)
        val serializedStr = evt.serializedStr
        val evt2 = evt.deserialize(serializedStr) as BotInitEvent
        assert(evt2.name == evt.name)
        println(evt2.teamId); println(evt.teamId)
        println(evt2.name); println(evt.name)
        assert(evt2.teamId == evt.teamId)
    }

    @Test
    fun testBotRequestEvent(){
        val colPoly = genRandomColPoly(10)
        val evt = BotRequestEvent("testReq", 1919, 810, arrayOf(1, colPoly, 3))
        val serializedStr = evt.serializedStr
        val evt2 = evt.deserialize(serializedStr) as BotRequestEvent
        assert(evt2.requestType == evt.requestType)
        assert(evt2.requestId == evt.requestId)
        assert(evt2.timeStamp == evt.timeStamp)
        assert(evt2.params.contentDeepEquals(evt.params))
    }
}