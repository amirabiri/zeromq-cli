package org.abiri.zeromqcli.zeromq

import org.abiri.zeromqcli.model.ZeromqEndpoint
import org.springframework.core.task.TaskExecutor
import org.zeromq.SocketType
import org.zeromq.ZContext

class ZeromqSubscriberSocket(endpoint: ZeromqEndpoint, zContext: ZContext, taskExecutor: TaskExecutor) :
    ZeromqMessageReceiverBase(endpoint, zContext, taskExecutor),
    ZeromqMessageReceiver {

    override val socketType: SocketType
        get() = SocketType.SUB

    override val server: Boolean
        get() = false

    override fun startSocket() {
        socket.connect(endpoint.endpoint)
        socket.subscribe("")
    }

    override fun receiveMessage(): Pair<String, String?> {
        val topic = socket.recvStr()
        val message = socket.recvStr()
        return Pair(message, topic)
    }
}