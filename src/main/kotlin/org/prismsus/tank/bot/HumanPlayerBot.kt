package org.prismsus.tank.bot
import io.ktor.websocket.*
import org.prismsus.tank.event.GameEvent
import org.prismsus.tank.event.GUIrequestEvent
import org.prismsus.tank.game.ControllerRequest
import org.prismsus.tank.game.OtherRequests
import org.prismsus.tank.networkings.WebSocketListener
import java.lang.Thread.interrupted
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class HumanPlayerBot(private val name : String, val webSockSession : DefaultWebSocketSession): GameBot {
    val evtsFromClnt : BlockingQueue<GUIrequestEvent> = LinkedBlockingQueue()
    val evtsToClnt : BlockingQueue<GameEvent> = LinkedBlockingQueue()
    val webSockListener : WebSocketListener
        get() = object : WebSocketListener {
            override fun onOpen() {
                println("Opened connection to $name")
            }
            override fun onMessage(text: String) {
                evtsFromClnt.add(GUIrequestEvent(text))
            }
            override fun onClose() {
                println("Closing connection to $name")
            }
            override fun onError(error: Throwable) {
                println("Failed to connect to $name")
            }
        }
    override fun loop(controller: FutureController) {
        while(true){
            if (evtsFromClnt.isEmpty()) continue
            val evt = evtsFromClnt.poll()
            when(evt.funName){
                "lTrack" -> {
                    controller.requestsQ.add(ControllerRequest(controller.cid, null, OtherRequests.SET_LTRACK_SPEED, arrayOf(evt.params[0] as Int), evt.time))
                }
                "rTrack" -> {
                    controller.requestsQ.add(ControllerRequest(controller.cid, null, OtherRequests.SET_RTRACK_SPEED, arrayOf(evt.params[0] as Int), evt.time))
                }
                "shoot" -> {
                    controller.requestsQ.add(ControllerRequest(controller.cid, null, OtherRequests.SHOOT, null, evt.time))
                }
            }
        }
    }
    override fun getName(): String {
        return name
    }
    override fun isUseFutureController(): Boolean {
        return true
    }
}