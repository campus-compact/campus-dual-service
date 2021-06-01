package service.utils

import java.io.OutputStreamWriter
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

internal object Utils {
    ////////////////////////
    // NETWORK OPERATIONS //
    ////////////////////////
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"
    private const val USER_AGENT2 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0"


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
}