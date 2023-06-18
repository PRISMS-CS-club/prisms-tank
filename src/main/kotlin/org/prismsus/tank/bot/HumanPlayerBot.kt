package org.prismsus.tank.bot
import okhttp3.*
import org.prismsus.tank.elements.GameElement
import org.prismsus.tank.event.GameEvent
import org.prismsus.tank.event.UserInputEvent
import java.lang.Thread.interrupted
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class HumanPlayerBot(private val name : String, val url : String): GameBot {
    val webSockRequest = Request.Builder().url(url).build()
    val webSockClnt = OkHttpClient()
    val evtsFromClnt : BlockingQueue<UserInputEvent> = LinkedBlockingQueue()
    val evtsToClnt : BlockingQueue<GameEvent> = LinkedBlockingQueue()
    val webSockListener : WebSocketListener
        get() = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("Opened connection to $name")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                evtsFromClnt.add(UserInputEvent(text))
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
            while(evtsFromClnt.isNotEmpty()){
                val evt = evtsFromClnt.poll()
                evt.shoot?.run{
                    controller.shoot()
                }
                evt.ltrackSpeed?.run{
                    controller.setLeftTrackSpeed(this)
                }
                evt.rtrackSpeed?.run{
                    controller.setRightTrackSpeed(this)
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