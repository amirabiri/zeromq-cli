package org.abiri.zeromqcli.controller

import jakarta.servlet.http.HttpServletRequest
import org.abiri.zeromqcli.service.ZeromqCliService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ZeromqCliController(
    private val cliSrv: ZeromqCliService
) {

    companion object {
        val DESTINATION = Regex("^/(?:(\\w+):)?(.*)$")
    }

    @PostMapping("/**")
    fun postMessage(request: HttpServletRequest, @RequestBody body: String): ResponseEntity<*> {
        val (endpointName, destination) = parseDestinationString(request)
        try {
            if (endpointName == null) {
                cliSrv.sendMessageToAll(destination, body)
            } else {
                cliSrv.sendMessageToEndpoint(endpointName, destination, body)
            }
            return ResponseEntity.ok().body("")
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }

    private fun parseDestinationString(request: HttpServletRequest) =
        DESTINATION.matchEntire(request.requestURI)!!.run {
            Pair(groupValues[1].ifEmpty { null }, groupValues[2])
        }

}