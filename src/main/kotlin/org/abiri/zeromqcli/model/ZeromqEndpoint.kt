package org.abiri.zeromqcli.model

import org.zeromq.SocketType

sealed interface ZeromqEndpoint {

    companion object {
        private val PREFIX_REGEX = Regex("^(?:(\\w+):)?(\\w+://.*)$")

        fun parse(socketType: SocketType, endpoint: String) =
            PREFIX_REGEX.matchEntire(endpoint)?.run {
                when {
                    groupValues[1].isEmpty() -> UnnamedZeromqEndpoint(socketType, groupValues[2])
                    else                     -> NamedZeromqEndpoint(socketType, groupValues[1], groupValues[2])
                }
            }
    }

    val socketType: SocketType
    val endpoint: String
    val displayName: String

    val isPub: Boolean
        get() = socketType == SocketType.PUB

    val isSub: Boolean
        get() = socketType == SocketType.SUB

    val isRouter: Boolean
        get() = socketType == SocketType.ROUTER

    val isDealer: Boolean
        get() = socketType == SocketType.DEALER

    val isUnnamed: Boolean
}
