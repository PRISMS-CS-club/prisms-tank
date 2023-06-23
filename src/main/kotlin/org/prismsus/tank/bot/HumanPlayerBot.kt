package org.prismsus.tank.bot
import okhttp3.*
import org.prismsus.tank.event.GameEvent
import org.prismsus.tank.event.GUIrequestEvent
import org.prismsus.tank.game.ControllerRequest
import org.prismsus.tank.game.OtherRequests
import java.lang.Thread.interrupted
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class HumanPlayerBot(private val name : String, val url : String): GameBot {
    val webSockRequest = Request.Builder().url(url).build()
    val webSockClnt = OkHttpClient()
    val evtsFromClnt : BlockingQueue<GUIrequestEvent> = LinkedBlockingQueue()
    val evtsToClnt : BlockingQueue<GameEvent> = LinkedBlockingQueue()
    val webSockListener : WebSocketListener
        get() = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("Opened connection to $name")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                evtsFromClnt.add(GUIrequestEvent(text))
            }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("Closing connection to $name")
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("Failed to connect to $name")
            }
        }
    val webSock : WebSocket = webSockClnt.newWebSocket(webSockRequest, webSockListener)
    val evtSendTh = Thread{
        while(true){
            while(evtsToClnt.isNotEmpty() && !interrupted()){
                val evt = evtsToClnt.poll()
                webSock.send(evt.serializedStr)
            }
        }
    }
    init{
        Runtime.getRuntime().addShutdownHook(Thread{
            webSock.close(1000, "Shutting down")
            evtSendTh.interrupt() }
        )
        evtSendTh.start()
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