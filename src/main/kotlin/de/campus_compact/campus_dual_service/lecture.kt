package de.campus_compact.campus_dual_service

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable

private const val SS_URL = "https://selfservice.campus-dual.de"
private const val CC_URL = "https://cc.mgutsche.de/auth/realms/master/protocol/openid-connect/userinfo"
private val client = HttpClient(CIO) {
    BrowserUserAgent()
    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        })
    }
}

@Serializable
data class Userinfo(
    val campus_dual_hash: String,
)

@Serializable
data class Lecture(
    val title: String,
    val start: String,
    val end: String,
    val allDay: Boolean,
    val description: String,
    val color: String,
    val editable: Boolean,
    val room: String,
    val sroom: String,
    val instructor: String,
    val sinstructor: String,
    val remarks: String,
)

suspend fun lecture(router: PipelineContext<Unit, ApplicationCall>) {
    val userinfo: Userinfo = try {
        client.post(CC_URL) {
            header("Authorization", router.call.request.headers["Authorization"])
        }
    } catch (e: ClientRequestException) {
        router.call.application.environment.log.error(e.toString())
        return router.call.respond(
            if (e.response.status == HttpStatusCode.Unauthorized)
                HttpStatusCode.Unauthorized
            else
                HttpStatusCode.InternalServerError
        )
    }

    val currentTime = System.currentTimeMillis()
    val lectures: List<Lecture> = client.get("$SS_URL/room/json?") {
        parameter("userid", router.call.parameters["username"])
        parameter("hash", userinfo.campus_dual_hash)
        parameter("start", currentTime - 60 * 60 * 24 * 14)
        parameter("end", currentTime + 60 * 60 * 24 * 31 * 12)
        parameter("_", currentTime)
    }

    router.call.respond(lectures)
}

