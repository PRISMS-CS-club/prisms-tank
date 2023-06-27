package org.prismsus.tank.networkings
import io.ktor.websocket.*

interface WebSocketListener {
    fun onOpen()
    fun onClose()
    fun onMessage(text : String)
    fun onError(error : Throwable)
}