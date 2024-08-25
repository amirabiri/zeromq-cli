package org.abiri.zeromqcli.zeromq

import org.zeromq.SocketType

interface ZeromqSocket {
    val socketType: SocketType
    var onConnectionStatusChanged: (connectionStatus: String) -> Unit
    fun start()
}