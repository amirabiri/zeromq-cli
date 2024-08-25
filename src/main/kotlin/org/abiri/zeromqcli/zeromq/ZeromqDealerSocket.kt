package org.abiri.zeromqcli.zeromq

import org.abiri.zeromqcli.model.ZeromqEndpoint
import org.springframework.core.task.TaskExecutor
import org.zeromq.SocketType
import org.zeromq.ZContext

class ZeromqDealerSocket(
    val clientId: String,
    endpoint: ZeromqEndpoint, zContext: ZContext, taskExecutor: TaskExecutor
) :
    ZeromqMessageReceiverBase(endpoint, zContext, taskExecutor),
    ZeromqMessageSender, ZeromqMessageReceiver {

    override val socketType: SocketType
        get() = SocketType.DEALER

    override val server: Boolean
        get() = false

    override fun startSocket() {
        socket.identity = clientId.toByteArray()
        socket.connect(endpoint.endpoint)
    }

    override fun receiveMessage(): Pair<String, String?> {
        val message = socket.recvStr()
        return Pair(message, null)
    }

    override fun send(message: String, destination: String) {
        socket.send(message)
    }
}