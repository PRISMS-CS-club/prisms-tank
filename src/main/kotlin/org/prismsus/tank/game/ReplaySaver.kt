package org.prismsus.tank.game

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.prismsus.tank.event.GameEvent
import org.prismsus.tank.utils.toJsonElement
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.PriorityBlockingQueue

class ReplaySaver(game: Game, private val file: File) {
    private val fileStream = file.outputStream()
    private val eventHistory = PriorityBlockingQueue<GameEvent>()
    private val replayThread: Thread

    init {
        fileStream.write("[\n".toByteArray())
        replayThread = Thread {
            while(game.running) {
                if(!eventHistory.isEmpty()) {
                    writeEvent(eventHistory.poll())
                }
            }
        }
    }

    fun save(event: GameEvent) {
        eventHistory.add(event)
    }

    private fun writeEvent(event: GameEvent) {
        fileStream.write(event.serializedBytes)
        fileStream.write(",\n".toByteArray())
    }

    fun stop() {
        replayThread.interrupt()
        while(!eventHistory.isEmpty()) {
            writeEvent(eventHistory.poll())
        }
    }

    fun postProcess() {
        val content = file.readText()
        file.writeText(if(content.length >= 2) content.substring(0, content.length - 2) else content)
        file.appendText("\n]")
    }
}