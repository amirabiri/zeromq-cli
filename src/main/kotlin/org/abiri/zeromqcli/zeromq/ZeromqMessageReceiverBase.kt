package org.abiri.zeromqcli.zeromq

import org.abiri.zeromqcli.model.ZeromqEndpoint
import org.springframework.core.task.TaskExecutor
import org.zeromq.ZContext
import org.zeromq.ZMQException
import zmq.ZError

abstract class ZeromqMessageReceiverBase(endpoint: ZeromqEndpoint, zContext: ZContext, taskExecutor: TaskExecutor) :
    ZeromqSocketBase(endpoint, zContext, taskExecutor),
    ZeromqMessageReceiver {

    override var onMessage: (message: String, source: String?) -> Unit =
        { _, _ ->  }

    override fun start() {
        createSocketAndStartMonitor()
        startSocket()
        taskExecutor.execute { receivingThread() }
    }

    private fun receivingThread() {
        try {
            while (true) {
                val (message, source) = receiveMessage()
                onMessage(message, source)
            }
        } catch (_: InterruptedException) {
        } catch (e: ZMQException) {
            if (e.errorCode != ZError.EINTR) {
                throw e
            }
        }
    }

    protected abstract fun receiveMessage(): Pair<String, String?>
}