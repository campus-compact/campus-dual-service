package service.lecture

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


private const val ERP_URL = "https://erp.campus-dual.de"
private const val SS_URL = "https://selfservice.campus-dual.de"
private const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"
const val USER_AGENT2 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0"


fun mainlecture(body: String): String {

    var bodyJson: JsonObject = Parser.default().parse(StringBuilder(body)) as JsonObject

    var username = bodyJson.getValue("username")
    var pwhash = bodyJson.getValue("hash")

    service.utils.Utils.allowAllCerts()

    var strResponse: String
    val currentTime = System.currentTimeMillis()
    val start = currentTime - 60 * 60 * 24 * 14
    val end = currentTime + 60 * 60 * 24 * 31 * 12
    var strURL: String = "$SS_URL/room/json?" +
            "userid=$username&" +
            "hash=$pwhash&" +
            "start=$start&" +
            "end=$end&" +
            "_=$currentTime"

    strResponse = service.utils.Utils.httpGet(strURL)

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

