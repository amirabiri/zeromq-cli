package org.abiri.zeromqcli.model

import org.zeromq.SocketType

data class UnnamedZeromqEndpoint(
    override val socketType: SocketType,
    override val endpoint: String
) : ZeromqEndpoint {

    override val displayName
        get() = endpoint

    override val isUnnamed
        get() = true
}