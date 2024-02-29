package org.prismsus.tank.bot

import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.event.BotInitEvent
import org.prismsus.tank.event.BotRequestEvent
import org.prismsus.tank.event.GameEvent
import org.prismsus.tank.networkings.TcpPackerClient
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.reflect.KProperty

class NetworkController(val ipAddr: String, val port: Int, val name: String, val tid: Long) {
    private val socket: Socket
    private val sockOutStream: OutputStream
    private val sockInStream: InputStream
    private val sockOutWriter: BufferedWriter
    private val sockInReader: BufferedReader
    private val eventQfromServer: Queue<GameEvent> = LinkedList()
    private val tcpPacker = TcpPackerClient(ipAddr, port)
    private val eventsWaitingResponse = mutableMapOf<Long, CompletableFuture<*>>()

    private val longUniqueRequestId : Long
        get(){
            // https://stackoverflow.com/questions/15184820/how-to-generate-unique-positive-long-using-uuid
            return (System.currentTimeMillis() shl 20) or (System.nanoTime() and 9223372036854251520L.inv())
        }
    private fun sendEvent(event: GameEvent) {
        tcpPacker.sendNonBlocking(event.serializedBytes)
    }

    inner class RemoteCallValueDelegate<T>{
        operator fun getValue(thisRef: Any?, property: KProperty<*>): CompletableFuture<T> {
            val ret = CompletableFuture<T>()
            val rid = longUniqueRequestId
            eventsWaitingResponse[rid] = ret
//            val req = BotRequestEvent(property.name,)
            return ret.thenApply {  it as T  }
        }
    }

    val visibleElements : Future<List<GameElement>>
        get(){
            val ret = CompletableFuture<Any>()
            val rid = longUniqueRequestId
            eventsWaitingResponse[rid] = ret
            TODO()
        }


    init {
        socket = Socket(ipAddr, port)
        sockOutStream = socket.getOutputStream()
        sockInStream = socket.getInputStream()
        sockOutWriter = socket.getOutputStream().bufferedWriter()
        sockInReader = socket.getInputStream().bufferedReader()
        val initEvent = BotInitEvent(name, tid)
        tcpPacker.startListenIncoming(); tcpPacker.startListenOutgoing()
        sendEvent(initEvent)
        val serverInitEvent = tcpPacker.incomingMessageQueue.removeAt(0)
    }
}