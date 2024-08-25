package org.abiri.zeromqcli.zeromq

import org.abiri.zeromqcli.model.ZeromqEndpoint
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Service
import org.zeromq.SocketType
import org.zeromq.ZContext

@Service
class ZeromqSocketFactory(
    private val zContext: ZContext,
    @Qualifier("applicationTaskExecutor")
    private val taskExecutor: TaskExecutor
) {

    fun createZeromqSocket(endpoint: ZeromqEndpoint, clientId: String): ZeromqSocket = when (endpoint.socketType) {
        SocketType.PUB    -> ZeromqPublisherSocket(endpoint, zContext, taskExecutor)
        SocketType.SUB    -> ZeromqSubscriberSocket(endpoint, zContext, taskExecutor)
        SocketType.ROUTER -> ZeromqRouterSocket(endpoint, zContext, taskExecutor)
        SocketType.DEALER -> ZeromqDealerSocket(clientId, endpoint, zContext, taskExecutor)

        else ->
            throw RuntimeException("Invalid ZeroMQ socket type: ${endpoint.socketType}")
    }
}