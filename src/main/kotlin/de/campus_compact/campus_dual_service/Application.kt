package de.campus_compact.campus_dual_service

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

var port: Int = (System.getenv("PORT") ?: "4321").toInt()

fun main() {
    embeddedServer(Netty, port = port) {
        install(CallLogging)
        install(ContentNegotiation) { json() }
        routing {
            get("/info") {
                call.respondText("Informationen folgen") //TODO -> show info file
            }
            post("/login") {
                login(this)
            }
            get("/lectures/{username}") {
                lecture(this)
            }
        }
    }.start(wait = true)
}