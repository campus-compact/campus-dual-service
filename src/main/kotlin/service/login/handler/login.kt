package service.login.handler

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import mu.KotlinLogging
import org.jsoup.Jsoup

private const val ERP_URL = "https://erp.campus-dual.de"
private const val SS_URL = "https://selfservice.campus-dual.de"
private var client = HttpClient()
private val logger = KotlinLogging.logger {}

fun initClient(){
    client = HttpClient(CIO) {
        install(HttpRedirect) {
            checkHttpMethod = false
        }
        install(HttpCookies) {
            // Will keep an in-memory map with all the cookies from previous requests.
            storage = AcceptAllCookiesStorage()
        }
    }
}

suspend fun mainlogin(body: String): String {
    initClient()

    //body -> {"username":400xxxx,"pw":"meinPW"}
    var bodyJson = JsonObject()
    try {
        bodyJson = Parser.default().parse(StringBuilder(body)) as JsonObject
    }catch (e : Exception){
        logger.error { "$e + \n>>> [Individual Message] There is no JsonObject in the body, check your syntax." }
        return  e.toString()
    }


    //initial Request to get the hidden fields (especially "sap-login-XSRF")
    val initUrl = ERP_URL + "/sap/bc/webdynpro/sap/zba_initss?" +
            "sap-client=100" +
            "&sap-language=de" +
            "&uri=https://selfservice.campus-dual.de/index/login"

    val initResponse: HttpResponse = client.get(initUrl)

    val initPage = Jsoup.parse(initResponse.readText())
    val hiddenInputs = initPage.select("#SL__FORM > input[type=hidden]")

    //login request
    var params = hashMapOf<String,String>()
    try {
        params = hashMapOf(
            "sap-user" to bodyJson.getValue("username").toString(),
            "sap-password" to bodyJson.getValue("pw").toString()
        )
    }catch (e : Exception){
        logger.error { "$e + \n>>> [Individual Message] There is no 'username' or 'pw' in body-json." }
        return e.toString()
    }

    for (input in hiddenInputs) {
        params[input.attr("name")] = input.attr("value")
    }

    val loginUrl = ERP_URL + initPage.select("#SL__FORM").attr("action")

    val response: HttpResponse = client.submitForm(
        url = loginUrl,
        formParameters = Parameters.build {
            params.forEach {
                append(it.key, it.value)
            }
        },
    )

    //Request of the main Page to get the hash needed to get a json calendar
    val mainResponse: HttpResponse = client.get("$SS_URL/index/login")

    val index = mainResponse.readText().indexOf(" hash=\"") // needs whitespace to match just one result
    return if (index != -1) {
        mainResponse.readText().substring(index + 7, index + 7 + 32)
    } else {
        logger.error { "No hash was included in the Response -> login data is probably wrong" }
        "No hash was included in the Response -> login data is probably wrong"
    }
}

