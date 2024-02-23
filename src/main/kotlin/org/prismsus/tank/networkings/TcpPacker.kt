package org.prismsus.tank.networkings

import org.prismsus.tank.utils.*
import java.net.Socket

class TcpPackerClient(val ipAddr : String, val port : Int) {
    private val socket = Socket(ipAddr, port)
    private val sockOutStream = socket.getOutputStream()
    private val sockInStream = socket.getInputStream()

    val incomingMessageQueue = mutableListOf<ByteArray>()
        get(){
            if (!isListeningIncoming)
                throw Exception("Did not start listening incoming message queue")
            return field
        }
    val outgoingMessageQueue = mutableListOf<ByteArray>() // message to be send
        get(){
            if (!isListeningOutgoing)
                throw Exception("Did not start listening outgoing message queue")
            return field
        }
    var isListeningIncoming = false
        private set
    var isListeningOutgoing = false
        private set

    fun startListenIncoming(){
        isListeningIncoming = true
        Thread {
            while(isListeningIncoming){
                val buffer = recv()
                val msg = buffer
                incomingMessageQueue.add(msg)
            }
        }.start()
    }

    fun stopListenIncoming(){
        isListeningIncoming = false
    }

    fun startListenOutgoing(){
        isListeningOutgoing = true
        Thread {
            while(isListeningOutgoing){
                if(outgoingMessageQueue.isNotEmpty()){
                    val msg = outgoingMessageQueue.removeAt(0)
                    send(msg)
                }
            }
        }.start()
    }

    fun stopListenOutgoing(){
        isListeningOutgoing = false
    }


    fun send(msg : ByteArray, cmd : Long = 0){
        val len = msg.size
        sockOutStream.write(len.toBytesBigEnd())
        sockOutStream.write(cmd.toBytesBigEnd())
        sockOutStream.write(msg)
    }

    fun send(msg : String, cmd : Long = 0){
        send(msg.toByteArray(), cmd)
    }

    fun sendNonBlocking(msg : ByteArray, cmd : Long = 0){
        outgoingMessageQueue.add(msg)
    }

    fun sendNonBlocking(msg : String, cmd : Long = 0){
        sendNonBlocking(msg.toByteArray(), cmd)
    }

    fun recv() : ByteArray{
        val len = sockInStream.readIntBigEnd()
        val cmd = sockInStream.readLongBigEnd()
        val buffer = ByteArray(len)
        sockInStream.read(buffer)
        return buffer
    }
}