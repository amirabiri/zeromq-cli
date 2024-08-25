package org.abiri.zeromqcli

import org.abiri.zeromqcli.command.ZeromqCliCommand
import org.abiri.zeromqcli.service.ZeromqCliReporter
import org.abiri.zeromqcli.service.ZeromqCliService
import org.abiri.zeromqcli.zeromq.ZeromqSocketFactory
import org.springframework.boot.Banner.Mode
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.zeromq.ZContext
import java.util.concurrent.CountDownLatch

val cliktCmd = ZeromqCliCommand()
fun main(args: Array<String>) {
    with(cliktCmd) {
        main(args)
        runApplication<ZeromqCliApplication> {
            setBannerMode(Mode.OFF)
            if (endpoints.all { it.isSub }) {
                webApplicationType = WebApplicationType.NONE
            }
            else {
                if (httpPort != null) {
                    setDefaultProperties(
                        mapOf(
                            "server.port" to httpPort.toString()
                        )
                    )
                }
            }
        }
        CountDownLatch(1).await()
    }
}

@SpringBootApplication
class ZeromqCliApplication {

    @Bean
    fun zContext() =
        ZContext()

    @Bean
    fun zeromqCliService(socketFactory: ZeromqSocketFactory, cliReporter: ZeromqCliReporter) = with(cliktCmd) {
        ZeromqCliService(socketFactory, cliReporter, endpoints, clientId)
    }

    @Bean
    fun zeromqCliReporter() = with(cliktCmd) {
        ZeromqCliReporter(endpoints.size > 1)
    }
}