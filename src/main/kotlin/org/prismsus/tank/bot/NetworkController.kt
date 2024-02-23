package org.prismsus.tank.bot

import org.prismsus.tank.event.*
import org.prismsus.tank.networkings.TcpPackerClient
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.*

class NetworkController(val ipAddr: String, val port: Int, val name: String, val tid: Long) {
    private val socket: Socket
    private val sockOutStream: OutputStream
    private val sockInStream: InputStream
    private val sockOutWriter: BufferedWriter
    private val sockInReader: BufferedReader
    private val eventQfromServer: Queue<GameEvent> = LinkedList()
    private val tcpPacker = TcpPackerClient(ipAddr, port)

    private fun sendEvent(event: GameEvent) {
        tcpPacker.sendNonBlocking(event.serializedBytes)
    }

    init {
        socket = Socket(ipAddr, port)
        sockOutStream = socket.getOutputStream()
        sockInStream = socket.getInputStream()
        sockOutWriter = socket.getOutputStream().bufferedWriter()
        sockInReader = socket.getInputStream().bufferedReader()
        sockInStream.read()
        val initEvent = BotInitEvent(name, tid)
        tcpPacker.startListenIncoming(); tcpPacker.startListenOutgoing()
        sendEvent(initEvent)
    }
}