package org.abiri.zeromqcli.model

import org.zeromq.SocketType

data class NamedZeromqEndpoint(
    override val socketType: SocketType,
    val name: String,
    override val endpoint: String
) : ZeromqEndpoint {

    override val displayName
        get() = name

    override val isUnnamed
        get() = false
}