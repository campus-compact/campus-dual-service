package service.rest

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jsoup.parser.Parser
import service.rest.plugins.configureRouting

var envVar:String = System.getenv("PORT") ?: "4321"

fun main() {
    embeddedServer(Netty, port = envVar.toInt()) {
        configureRouting()
    }.start(wait = true)
}