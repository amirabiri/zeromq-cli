package org.abiri.zeromqcli.service

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.terminal.Terminal
import org.abiri.zeromqcli.model.ZeromqEndpoint

class ZeromqCliReporter(
    private val showEndpointNames: Boolean
) {

    private val t = Terminal()

    fun notice(message: String) {
        t.println(gray(message))
    }

    fun warn(message: String) {
        t.println(red(message))
    }

    fun connectionStatus(endpoint: ZeromqEndpoint, status: String) {
        t.println(gray(formatLine(endpoint.displayName, "CONN", status)))
    }

    fun messageEvent(endpoint: ZeromqEndpoint, eventType: String, destination: String?, message: String) {
        messageEvent(endpoint.displayName, eventType, destination, message)
    }

    fun messageEventAll(eventType: String, destination: String?, message: String) {
        messageEvent("*", eventType, destination, message)
    }

    private fun messageEvent(endpointDescription: String, eventType: String, destination: String?, message: String) {
        if (destination != null) {
            t.println(formatLine(blue(endpointDescription), brightGreen(eventType), "${brightYellow(destination)} : ${message.replaceSpecialChars()}"))
        } else {
            t.println(formatLine(blue(endpointDescription), brightGreen(eventType), message.replaceSpecialChars()))
        }
    }

    private fun String.replaceSpecialChars() = buildString {
        for (c in this@replaceSpecialChars) {
            when {
                c == '\n' -> append("\\n")
                c == '\r' -> append("\\r")
                c == '\t' -> append("\\t")
                c.isISOControl() -> append("\\u%04x".format(c.code))
                else -> append(c)
            }
        }
    }

    private fun formatLine(endpointName: String, prefix: String, message: String) = buildString {
        append(prefix)
        if (showEndpointNames) {
            append(" ["); append(endpointName); append("]")
        }
        append(" - ")
        append(message)
    }
}