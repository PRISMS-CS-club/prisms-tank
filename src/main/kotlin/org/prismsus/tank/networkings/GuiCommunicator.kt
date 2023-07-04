package org.prismsus.tank.networkings

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.prismsus.tank.bot.HumanPlayerBot
import org.prismsus.tank.utils.game
import org.prismsus.tank.utils.gameMap
import java.util.concurrent.CompletableFuture

class GuiCommunicator(val clntCnt: Int) {
    fun start() {
        embeddedServer(Netty, port = 1145) {
            module()
        }.start(wait = false)
    }

    fun Application.module() {
        configureSockets()
        configureRouting()
    }

    fun Application.configureSockets() {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun Application.configureRouting() {
        val logger = log
        routing {
            webSocket("/") {

                val name = incoming.receive().data.toString(Charsets.UTF_8)
                if (name == "") {
                    // TODO (add player in observer mode)
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Empty name"))
                    return@webSocket
                } else if (m_humanPlayerBots.size >= clntCnt) {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Too many clients"))
                    return@webSocket
                }


                val newBot = HumanPlayerBot(name, this)
                m_humanPlayerBots.add(newBot)
                if (m_humanPlayerBots.size == clntCnt) {
                    humanPlayerBots.complete(m_humanPlayerBots)
                }

                GlobalScope.launch() {
                    while (true) {
                        if (newBot.evtsToClnt.isEmpty()) {
                            continue
                        }
                        val evt = newBot.evtsToClnt.poll()
//                        println("Sent: ${evt.serializedStr}")
                        send(evt.serializedStr)
                    }
                }.start()

                for (frame in incoming) {
                    // ignore if the bot is killed in the game
                    if (!game!!.bots.contains(newBot))
                        continue
                    frame as Frame.Text
                    val msg = frame.readText()
//                    println("Received: $msg")
                    newBot.webSockListener.onMessage(msg)
                }
            }
        }
    }

    private val m_humanPlayerBots: ArrayList<HumanPlayerBot> = ArrayList<HumanPlayerBot>()
    val humanPlayerBots: CompletableFuture<ArrayList<HumanPlayerBot>> = CompletableFuture<ArrayList<HumanPlayerBot>>()
}
