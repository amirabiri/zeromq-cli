package org.abiri.zeromqcli.zeromq

interface ZeromqMessageReceiver : ZeromqSocket {

    var onMessage: (message: String, source: String?) -> Unit

}