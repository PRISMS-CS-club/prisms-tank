package org.prismsus.tank.networkings
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.prismsus.tank.bot.HumanPlayerBot
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class GUIcommunicator(val clntCnt : Int){
    fun start(){
        embeddedServer(Netty, port = 1145){
            module()
        }.start(wait = false)
    }
    fun Application.module(){
        configureSockets()
        configureRouting()
    }

    fun Application.configureSockets(){
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
    }
    fun Application.configureRouting(){
        val logger = log
        routing{
            webSocket("/") {
                if (m_humanPlayerBots.size >= clntCnt){
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Too many clients"))
                    if (m_humanPlayerBots.size == clntCnt)
                        humanPlayerBots.complete(m_humanPlayerBots)
                    return@webSocket
                }
                val newBot = HumanPlayerBot( "bot[${m_humanPlayerBots.size}]",this)
                m_humanPlayerBots.add(newBot)
                launch {
                    incoming.consumeEach {
                        frame ->
                        frame as Frame.Text
                        val msg = frame.readText()
                        newBot.webSockListener.onMessage(msg)
                    }
                }
                while(true){
                    for (evt in newBot.evtsToClnt){
                        println("Sent: ${evt.serializedStr}")
                        send(evt.serializedStr)
                    }
                }
            }
        }
    }
    private val m_humanPlayerBots : ArrayList<HumanPlayerBot> = ArrayList<HumanPlayerBot>()
    val humanPlayerBots : CompletableFuture<ArrayList<HumanPlayerBot>> = CompletableFuture<ArrayList<HumanPlayerBot>>()
}
