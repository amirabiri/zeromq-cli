package org.abiri.zeromqcli.service

import org.abiri.zeromqcli.model.NamedZeromqEndpoint
import org.abiri.zeromqcli.model.ZeromqEndpoint
import org.abiri.zeromqcli.util.asMutable
import org.abiri.zeromqcli.zeromq.ZeromqMessageReceiver
import org.abiri.zeromqcli.zeromq.ZeromqMessageSender
import org.abiri.zeromqcli.zeromq.ZeromqSocket
import org.abiri.zeromqcli.zeromq.ZeromqSocketFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.web.context.WebServerInitializedEvent
import org.springframework.context.event.EventListener
import org.zeromq.SocketType
import java.util.*

class ZeromqCliService(
    private val socketFactory: ZeromqSocketFactory,
    private val cliReporter: ZeromqCliReporter,
    private val endpoints: List<ZeromqEndpoint>,
    private val clientIdParam: String?,
) {
    private val allSockets: List<ZeromqSocket> = ArrayList()
    private val senderSockets: List<ZeromqMessageSender> = ArrayList()
    private val namedSockets: Map<String, Pair<ZeromqEndpoint, ZeromqSocket>> = HashMap()

    private lateinit var sendToAllEventType: String
    private lateinit var clientId: String

    @EventListener(ApplicationReadyEvent::class)
    fun onStart() {
        handleDealerClientId()
        for (endpoint in endpoints) {
            createZeromqSocket(endpoint)
        }
        sendToAllEventType = determineSendToAllEventType()
    }

    @EventListener
    fun onWebServerInitialized(evt: WebServerInitializedEvent) {
        cliReporter.notice("HTTP Port: ${evt.webServer.port}")
    }

    private fun handleDealerClientId() {
        if (clientIdParam != null) {
            clientId = clientIdParam
        }
        else if (endpoints.any { it.isDealer && it.isUnnamed })
        {
            clientId = generateRandomClientId()
            cliReporter.notice("Auto-generated client id: $clientId")
        }
    }

    private fun generateRandomClientId() =
        UUID.randomUUID().toString().replace("-", "").substring(0, 12)

    private fun createZeromqSocket(endpoint: ZeromqEndpoint) {
        val socket = socketFactory.createZeromqSocket(endpoint, clientIdForEndpoint(endpoint))
        allSockets.asMutable() += socket
        if (endpoint is NamedZeromqEndpoint) {
            namedSockets.asMutable()[endpoint.name] = Pair(endpoint, socket)
        }
        socket.onConnectionStatusChanged = { connectionStatus -> onConnectionStatusChanged(endpoint, connectionStatus) }
        if (socket is ZeromqMessageReceiver) {
            socket.onMessage = { message, source -> onMessageReceived(endpoint, message, source) }
        }
        if (socket is ZeromqMessageSender) {
            senderSockets.asMutable() += socket
        }
        socket.start()
    }

    private fun clientIdForEndpoint(endpoint: ZeromqEndpoint) = when {
        endpoint.isDealer -> when {
            clientIdParam != null           -> clientId
            endpoint is NamedZeromqEndpoint -> endpoint.name
            else                            -> clientId
        }
        else -> ""
    }

    private fun onConnectionStatusChanged(endpoint: ZeromqEndpoint, connectionStatus: String) {
        cliReporter.connectionStatus(endpoint, connectionStatus)
    }

    private fun onMessageReceived(endpoint: ZeromqEndpoint, message: String, source: String?) {
        cliReporter.messageEvent(endpoint, socketTypeReceivePrefix(endpoint.socketType), source, message)
    }

    private fun determineSendToAllEventType() =
        senderSockets.map { it.socketType }.toSet().run {
            when {
                size == 1 -> socketTypeSendPrefix(first())
                else      -> "X   "
            }
        }

    fun sendMessageToAll(destination: String, message: String) {
        if (senderSockets.isEmpty()) {
            throw IllegalArgumentException("None of the endpoints are capable of sending messages")
        }
        cliReporter.messageEventAll(sendToAllEventType, destination, message)
        for (socket in senderSockets) {
            socket.send(message, destination)
        }
    }

    fun sendMessageToEndpoint(endpointName: String, destination: String, message: String) {
        if (senderSockets.isEmpty()) {
            throw IllegalArgumentException("None of the endpoints are capable of sending messages")
        }
        val (endpoint, socket) = namedSockets[endpointName]
            ?: throw IllegalArgumentException("Unknown endpoint: $endpointName")
        if (socket !is ZeromqMessageSender) {
            throw IllegalArgumentException("Endpoints of type ${socket.socketType} cannot send messages")
        }
        cliReporter.messageEvent(endpoint, socketTypeSendPrefix(socket.socketType), destination, message)
        socket.send(message, destination)
    }

    private fun socketTypeSendPrefix(socketType: SocketType) = when (socketType) {
        SocketType.PUB    -> "PUB "
        SocketType.ROUTER -> "SEND"
        SocketType.DEALER -> "SEND"

        else ->
            throw IllegalArgumentException("Unexpected socket type: $socketType")
    }

    private fun socketTypeReceivePrefix(socketType: SocketType) = when (socketType) {
        SocketType.SUB    -> "SUB "
        SocketType.ROUTER -> "RECV"
        SocketType.DEALER -> "RECV"

        else ->
            throw IllegalArgumentException("Unexpected socket type: $socketType")
    }
}