package service.rest.plugins

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import service.lecture.mainlecture
import service.login.handler.mainlogin

fun Application.configureRouting() {
    // Starting point for a Ktor app:
    routing {
        get("/") {
            call.respondText("Hallo lieber User :>. \nJa, deine Anfrage hat funktioniert, aber ohne einen Pfad (wie /test), bekommst du hier nichts XD.")
        }
        get("/test") {
            call.respondText("Hello World! Hier ist der Testtext :D")
        }
        get("/info") {
            call.respondText("Informationen folgen") //TODO -> show info file
        }
        post("/login") {
            val body = call.receive<String>()
            var response = ""
            try {
                response = mainlogin(body)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            call.respondText(response)
        }
        post("/lecture") {
            val body = call.receive<String>()
            val headers = call.request.headers
            call.respondText(mainlecture(body,headers))
        }
    }

}
