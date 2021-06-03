package service.lecture

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import mu.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

private const val ERP_URL = "https://erp.campus-dual.de"
private const val SS_URL = "https://selfservice.campus-dual.de"
private const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"
const val USER_AGENT2 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0"
private val logger = KotlinLogging.logger {}

suspend fun mainlecture(body: String, headers: Headers): String {
    var bodyJson: JsonObject = Parser.default().parse(StringBuilder(body)) as JsonObject
    var username = bodyJson.getValue("username")
    var token = headers["Authorization"].toString()

    allowAllCerts()

    var hash = getHashFromKeycloak(token)

    var strResponse: String
    val currentTime = System.currentTimeMillis()
    val start = currentTime - 60 * 60 * 24 * 14
    val end = currentTime + 60 * 60 * 24 * 31 * 12
    var strURL: String = "$SS_URL/room/json?" +
            "userid=$username&" +
            "hash=$hash&" +
            "start=$start&" +
            "end=$end&" +
            "_=$currentTime"

    strResponse = httpGet(strURL)

    val jArrResponse: JsonArray<JsonObject> = Parser.default().parse(StringBuilder(strResponse)) as JsonArray<JsonObject>

    //Rausschmeißer nach date
    var dateStart: Long
    var dateEnd: Long

    try{
        try {
            //Format -> Milliseconds
            dateStart = bodyJson.getValue("start").toString().toLong()
            dateEnd = bodyJson.getValue("end").toString().toLong()
        } catch (e: Exception) {
            //Format -> "YYYY-MM-DD HH-MM"
            dateStart = DateStrToMilli(bodyJson.getValue("start").toString())
            dateEnd = DateStrToMilli(bodyJson.getValue("end").toString())
        }
    }catch (e : Exception){ //if start & end = "", or not available
        return jArrResponse.toJsonString()
    }

    val jArrLectures: JsonArray<JsonObject> = JsonArray()
    for (jObj: JsonObject in jArrResponse) {
        val dateStartLoc = DateStrToMilli(jObj.getValue("start").toString())
        val dateEndLoc = DateStrToMilli(jObj.getValue("end").toString())

        if (!(dateStartLoc < dateStart || dateEndLoc > dateEnd)) jArrLectures.add(jObj)
    }

    return jArrLectures.toJsonString()
}

private fun DateStrToMilli(strDate: String): Long { //From "2018-12-01 15:10" TO "1514764800000"
    var strArrDate = strDate.split(" ")
    val date = LocalDate.parse(strArrDate[0], DateTimeFormatter.ISO_LOCAL_DATE)
    return date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
}

fun httpGet(urlString: String): String {
    //Make the actual connection
    val url = URL(urlString)
    val urlConnection = url.openConnection() as HttpsURLConnection
    urlConnection.setRequestProperty("User-Agent", USER_AGENT2)

    //get and return inputStream (converted to a String)
    val responseString = urlConnection.inputStream.bufferedReader().readText()
    urlConnection.disconnect()
    return responseString
}

fun allowAllCerts() {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate?> {
            return arrayOfNulls(0)
        }

        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {
        }

        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {
        }
    })

    val sc = SSLContext.getInstance("SSL")
    sc.init(null, trustAllCerts, SecureRandom())
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
    HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
}

private suspend fun getHashFromKeycloak(token : String) : String {
    val client = HttpClient(CIO)
    val response = client.post<HttpResponse>("https://cc.mgutsche.de/auth/realms/master/protocol/openid-connect/userinfo") {
        // Configure request parameters exposed by HttpRequestBuilder
        header("Authorization",token)
        userAgent(USER_AGENT2)
    }
    client.close()

    val jObj: JsonObject = Parser.default().parse(StringBuilder(response.readText())) as JsonObject

    return jObj.getValue("campus-dual-hash").toString()
}

