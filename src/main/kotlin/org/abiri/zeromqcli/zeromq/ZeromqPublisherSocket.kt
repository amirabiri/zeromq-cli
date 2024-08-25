package org.abiri.zeromqcli.zeromq

import org.abiri.zeromqcli.model.ZeromqEndpoint
import org.springframework.core.task.TaskExecutor
import org.zeromq.SocketType
import org.zeromq.ZContext

class ZeromqPublisherSocket(endpoint: ZeromqEndpoint, zContext: ZContext, taskExecutor: TaskExecutor) :
    ZeromqSocketBase(endpoint, zContext, taskExecutor),
    ZeromqMessageSender {

    override val socketType: SocketType
        get() = SocketType.PUB

    override val server: Boolean
        get() = true

    override fun startSocket() {
        socket.bind(endpoint.endpoint)
    }

    override fun send(message: String, destination: String) {
        socket.sendMore(destination)
        socket.send(message)
    }
}