package org.abiri.zeromqcli.zeromq

import org.abiri.zeromqcli.model.ZeromqEndpoint
import org.springframework.core.task.TaskExecutor
import org.zeromq.SocketType
import org.zeromq.ZContext

class ZeromqRouterSocket(endpoint: ZeromqEndpoint, zContext: ZContext, taskExecutor: TaskExecutor) :
    ZeromqMessageReceiverBase(endpoint, zContext, taskExecutor),
    ZeromqMessageSender, ZeromqMessageReceiver {

    override val socketType: SocketType
        get() = SocketType.ROUTER

    override val server: Boolean
        get() = true

    override fun startSocket() {
        socket.bind(endpoint.endpoint)
    }

    override fun receiveMessage(): Pair<String, String?> {
        val identity = socket.recvStr()
        val message = socket.recvStr()
        return Pair(message, identity)
    }

    override fun send(message: String, destination: String) {
        socket.sendMore(destination)
        socket.send(message)
    }
}