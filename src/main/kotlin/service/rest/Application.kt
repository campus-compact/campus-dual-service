package service.rest

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import service.rest.plugins.configureRouting

fun main() {
    embeddedServer(Netty, port = 4321) {
        configureRouting()
    }.start(wait = true)
}