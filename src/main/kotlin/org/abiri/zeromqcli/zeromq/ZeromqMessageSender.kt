package org.abiri.zeromqcli.zeromq

interface ZeromqMessageSender : ZeromqSocket {

    fun send(message: String, destination: String)

}