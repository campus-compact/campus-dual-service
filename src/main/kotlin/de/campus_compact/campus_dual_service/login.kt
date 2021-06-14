package de.campus_compact.campus_dual_service

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup

// Campus Dual URLs
private const val ERP_URL = "https://erp.campus-dual.de"
private const val SS_URL = "https://selfservice.campus-dual.de"

// Http Client for requests to campus dual
private val client = HttpClient(CIO) {
    install(HttpRedirect) { checkHttpMethod = false }
    install(HttpCookies) { storage = AcceptAllCookiesStorage() }
}

// Regex to parse data from studentInfo
private val lastName = Regex("(?<=Name: )(.*(?=, .* \\([0-9]+\\),))")
private val firstName = Regex("(?<=, )(.*(?= \\([0-9]+\\),))")
private val seminargruppe = Regex("(?<=Seminargruppe: )(.*(?= Studiengang ))")
private val studiengang = Regex("(?<=Studiengang )(.*(?=/Studienrichtung ))")
private val studienrichtung = Regex("(?<=/Studienrichtung ).*")

// Request this functions works with
@Serializable
data class Request(
    val username: String,
    val password: String,
)

// Response this function works with
@Serializable
data class Response(
    val hash: String,
    val firstName: String?,
    val lastName: String?,
    val seminargruppe: String?,
    val studiengang: String?,
    val studienrichtung: String?,
)

suspend fun login(router: PipelineContext<Unit, ApplicationCall>) {
    // Get the request body
    val req = router.call.receive<Request>()

    // Initial Request to later get the hidden from fields (especially "sap-login-XSRF")
    val initResponse: HttpResponse = client.get("$ERP_URL/sap/bc/webdynpro/sap/zba_initss") {
        parameter("sap-client", "100")
        parameter("sap-language", "de")
        parameter("uri", "https://selfservice.campus-dual.de/index/login")
    }

    // Submit the form on that page to get a session.
    // The necessary cookies are set automatically, so we can ignore the response.
    val initPage = Jsoup.parse(initResponse.readText())
    val hiddenInputs = initPage.select("#SL__FORM input[type=hidden]")
    client.submitForm<HttpResponse>(
        url = ERP_URL + initPage.select("#SL__FORM").attr("action"),
        formParameters = Parameters.build {
            append("sap-user", req.username)
            append("sap-password", req.password)
            hiddenInputs.forEach {
                append(it.attr("name"), it.attr("value"))
            }
        },
    )

    // Request the main Page to verify the login was successful and get all the userinfo we want.
    val mainResponse: HttpResponse = client.get("$SS_URL/index/login")
    val mainBody = mainResponse.readText()
    val hashIndex = mainBody.indexOf(" hash=\"") // needs whitespace to match just one result
    if (hashIndex != -1) {
        val studentInfo = Jsoup.parse(mainBody).select("#studinfo").text()
        router.call.respond(
            Response(
                hash = mainBody.substring(hashIndex + 7, hashIndex + 7 + 32),
                firstName = firstName.find(studentInfo)?.value,
                lastName = lastName.find(studentInfo)?.value,
                seminargruppe = seminargruppe.find(studentInfo)?.value,
                studiengang = studiengang.find(studentInfo)?.value,
                studienrichtung = studienrichtung.find(studentInfo)?.value,
            )
        )
    } else {
        router.call.respond(HttpStatusCode.Unauthorized)
    }
}
