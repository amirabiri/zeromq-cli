package org.abiri.zeromqcli.zeromq

import org.abiri.zeromqcli.model.ZeromqEndpoint
import org.springframework.core.task.TaskExecutor
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import org.zeromq.ZMonitor

abstract class ZeromqSocketBase(
    protected val endpoint: ZeromqEndpoint,
    private val zContext: ZContext,
    protected val taskExecutor: TaskExecutor
) : ZeromqSocket {

    protected lateinit var socket: ZMQ.Socket

    protected abstract val server: Boolean

    override var onConnectionStatusChanged: (connectionStatus: String) -> Unit =
        { }

    override fun start() {
        createSocketAndStartMonitor()
        startSocket()
    }

    protected fun createSocketAndStartMonitor() {
        socket = zContext.createSocket(socketType)
        val monitor = createMonitor()
        taskExecutor.execute { monitorThread(monitor) }
    }

    protected abstract fun startSocket()

    private fun createMonitor(): ZMonitor {
        val monitor = ZMonitor(zContext, socket)
        monitor.add(ZMonitor.Event.LISTENING, ZMonitor.Event.ACCEPTED, ZMonitor.Event.CONNECTED, ZMonitor.Event.DISCONNECTED)
        monitor.start()
        return monitor
    }

    private fun monitorThread(monitor: ZMonitor) {
        while (true) {
            val event = monitor.nextEvent()
                ?: return
            val connStatus = when (event.type) {
                ZMonitor.Event.LISTENING    -> "Listening"
                ZMonitor.Event.ACCEPTED     -> "Client connected"
                ZMonitor.Event.CONNECTED    -> "Connected"
                ZMonitor.Event.DISCONNECTED -> if (server) "Client disconnected" else "Disconnected"
                else                        -> null
            }
            if (connStatus != null) {
                onConnectionStatusChanged(connStatus)
            }
        }
    }
}