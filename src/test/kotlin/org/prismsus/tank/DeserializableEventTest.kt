package org.prismsus.tank

import org.junit.jupiter.api.Test
import org.prismsus.tank.event.ServerResponseEvent
import org.prismsus.tank.utils.collidable.ColPoly
import org.prismsus.tank.utils.collidable.DPos2
import org.prismsus.tank.utils.deepCopyByKyro
import org.prismsus.tank.utils.deserializeByKyro
import org.prismsus.tank.utils.serializeByKyro
import java.util.zip.Deflater
import java.util.zip.Deflater.BEST_SPEED

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
    fun testServerResponseEvent(){
        val colPoly = genRandomColPoly(5)
        val evt = ServerResponseEvent(colPoly, 1145, 1919810, -1)
        val serializedStr = evt.serializedStr
        println(serializedStr)
        // print the size of the serialized string
        println(serializedStr.length)
        // check the deflated size
        val defalter = Deflater(BEST_SPEED)
        defalter.setInput(serializedStr.toByteArray())
        defalter.finish()
        val deflatedBytes = ByteArray(1024)
        val deflatedSize = defalter.deflate(deflatedBytes)
        println(deflatedSize)
        val serializedBytes = evt.serializedBytes
        val evt2 = evt.deserialize(serializedStr) as ServerResponseEvent
        assert(evt2.timeStamp == evt.timeStamp)
        assert(evt2.serialName == evt.serialName)
        assert(evt2.returnValue == evt.returnValue)
    }
}