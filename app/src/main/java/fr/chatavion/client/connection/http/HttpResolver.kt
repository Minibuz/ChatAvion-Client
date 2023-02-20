package fr.chatavion.client.connection.http

import android.util.Log
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class HttpResolver {

    var id: Int = 0

    fun requestHistory(
        community: String,
        address: String,
        nbMessage: Int,
    ) : List<String> {
        val url = URL("http://chat.$address/history/$community/$id?amount=$nbMessage")

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"  // optional default is GET

            println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

            inputStream.bufferedReader().use {
                it.lines().forEach { line ->
                    // Parse le résultat pour avoir les
                    // message
                    println(line)
                }
            }
        }
        return listOf()
    }

    fun sendMessage(
        community: String,
        address: String,
        pseudo: String,
        message: String,
    ) : Boolean {
        val msgAsBytes = message.toByteArray(StandardCharsets.UTF_8)
        if (msgAsBytes.size > 35) {
            Log.w("HttpSender","Message cannot be more than 35 character as UTF_8 byte array.")
            return false
        }
        val url = URL("http://chat.$address/message/$community")

        val payload = "{\"username\": \"$pseudo\", \"message\": \"$message\"}"

        with(url.openConnection() as HttpURLConnection) {
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Content-Length", payload.length.toString())
            doOutput = true

            val wr = OutputStreamWriter(outputStream)
            wr.write(payload)
            wr.flush()

            println("URL : $url")
            println("Response Code : $responseCode")

            inputStream.bufferedReader().use {
                it.lines().forEach { line ->
                    println(line)
                }
            }
        }
        return true
    }

    fun communityChecker (
        address : String,
        community: String
    ) : Boolean {
        val url = URL("http://chat.$address/community/$community")

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"  // optional default is GET

            println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

            inputStream.bufferedReader().use {
                it.lines().forEach { line ->
                    // Parse le résultat pour avoir le
                    // résultat, true ou false
                    println(line)
                }
            }
        }
        return false
    }
}