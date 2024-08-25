package org.abiri.zeromqcli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import org.abiri.zeromqcli.model.ZeromqEndpoint
import org.zeromq.SocketType

class ZeromqCliCommand : CliktCommand(
    name = "zeromq",
    help = "A simple ZeroMQ command line development tool that can act as a peer of various socket types.",
    epilog =
        "At least one endpoint must be given. Currently only PUB, SUB, ROUTER and DEALER socket types are " +
        "supported (represented respectively by the --pub|--publisher, --sub|--subscriber, --router and --dealer " +
        "options. Endpoints are given in ZeroMq standard format and and can be optionally prefixed by a name using " +
        "[name]:[endpoint] notation. For listening socket types (PUB and ROUTER) the endpoint host must be " +
        "localhost, 0.0.0.0 or 127.0.0.1\n\n" +

        "If any of the given endpoints are of a sending-capable socket type (PUB, ROUTER, DEALER), an HTTP " +
        "interface will be created through which messages can be sent. By default the HTTP server will listen on " +
        "port 8080, and this port can be set using the -p|--http-port option.\n\n" +

        "Sending messages through the HTTP interface is simple - any POST HTTP request is interpreted as a message " +
        "to send, with the request body being the message payload, and the URL path as the destination. The meaning " +
        "of the destination depends on the socket type: topic for PUB, client id for ROUTER, and ignored for DEALER. " +
        "The message will be sent through all sending-capable endpoints. To target a specific endpoint, the URL path " +
        "can be prefixed by the endpoint name.\n\n" +

        "DEALER endpoints require a client id. The client id used will be determined in the following order:\n" +
        " - If --client-id is given, it is always used for all DEALER endpoints.\n" +
        " - If no --client-id is given, named endpoints will use the name as the client id.\n" +
        " - For any non-named DEALER endpoints when --client-id isn't given, a random client id will be generated.\n\n" +

        "Examples:\n\n" +

        "Start a simple PUB socket on port 5559 (HTTP interface will be created on port 8080):\n" +
        "$ zeromq --pub tcp://localhost:5559\n\n" +

        "Open two SUB sockets to two separate endpoints, giving them appropriate names (no HTTP interface will be " +
        "created):\n" +
        "$ zeromq --sub service-a:tcp://api.service-a.org:5559 --sub service-b:tcp://another-service.dev:10000\n\n" +

        "Create a simple ROUTER socket and set the HTTP port to 9090:\n" +
        "$ zeromq --router tcp://localhost:5555 -p 9090\n\n" +

        "Create a DEALER socket with an auto generated client id:\n" +
        "$ zeromq --dealer tcp://localhost:5555\n\n",
    printHelpOnEmptyArgs = true
) {

    val endpoints by option("--publish", "--pub", "--subscribe", "--sub", "--router", "--dealer", metavar = "endpoint")
        .convert(metavar = "endpoint") { parseEndpoint(it) }
        .multiple()
        .help("ZeroMQ endpoints")

    val httpPort: Int? by option("--http-port", "-p", metavar = "http port")
        .int()
        .help("Http port to listen on")

    val clientId: String? by option("--client-id", metavar = "client id")
        .help("ZeroMQ client id for dealer connections. If not specified a random GUID will be used")

    private fun OptionCallTransformContext.parseEndpoint(endpointStr: String) =
        ZeromqEndpoint.parse(endpointType(name), endpointStr)
            ?: throw CliktError("'$endpointStr' is not a valid ZeroMQ endpoint")

    private fun endpointType(name: String) = when (name) {
        "--publish"   -> SocketType.PUB
        "--pub"       -> SocketType.PUB
        "--subscribe" -> SocketType.SUB
        "--sub"       -> SocketType.SUB
        "--router"    -> SocketType.ROUTER
        "--dealer"    -> SocketType.DEALER

        else ->
            throw CliktError("Unknown endpoint type")
    }

    override fun run() {
        if (endpoints.isEmpty()) {
            throw CliktError("Must specify at least one ZeroMQ endpoint")
        }

        if (endpoints.all { it.isSub } && httpPort != null) {
            throw CliktError("SUB endpoints cannot send messages, so no HTTP port can be used")
        }
    }
}