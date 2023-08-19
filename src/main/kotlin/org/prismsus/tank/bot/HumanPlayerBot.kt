package org.prismsus.tank.bot
import io.ktor.websocket.*
import org.prismsus.tank.event.GameEvent
import org.prismsus.tank.event.GUIrequestEvent
import org.prismsus.tank.game.ControllerRequest
import org.prismsus.tank.game.OtherRequests
import org.prismsus.tank.networkings.WebSocketListener
import org.prismsus.tank.utils.game
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class HumanPlayerBot(private val name : String, val webSockSession : DefaultWebSocketSession, val isObserver : Boolean = false): GameBot {
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
                    controller.requestsQ.add(ControllerRequest(controller.cid, null, OtherRequests.SET_LTRACK_SPEED, arrayOf((evt.params[0] as Number).toDouble()), evt.timeStamp))
                }
                "rTrack" -> {
                    controller.requestsQ.add(ControllerRequest(controller.cid, null, OtherRequests.SET_RTRACK_SPEED, arrayOf((evt.params[0] as Number).toDouble()), evt.timeStamp))
                }
                "fire" -> {
                    controller.requestsQ.add(ControllerRequest(controller.cid, null, OtherRequests.FIRE, null, evt.timeStamp))
                }
                else -> {
                    // find the prefix before .
                    val dotIdx = evt.funName.indexOf('.')
                    if (dotIdx == -1) throw Exception("invalid event name")
                    val prefix = evt.funName.substring(0, dotIdx)
                    val suffix = evt.funName.substring(dotIdx + 1)
                    when (prefix) {
                        "market" -> {
                            game!!.marketImpl.processGUIevts(controller.cid, suffix, evt.params)
                        }
                    }
                }
            }
        }
    }
    override fun getName(): String {
        return name
    }
    override fun isFutureController(): Boolean {
        return true
    }
}