package fr.chatavion.client.connection.http

import android.util.Log
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import com.fasterxml.jackson.module.kotlin.*
import java.io.IOException

class HttpResolver {

    private val mapper = jacksonObjectMapper()
    var id: Int = 0
    var isConnected: Boolean = false

    fun requestHistory(
        community: String,
        address: String,
        nbMessage: Int,
    ) : List<String> {
        val url = URL("http://chat.$address/history/$community/$id?amount=$nbMessage")
        val mutableList: MutableList<String> = mutableListOf()

        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"  // optional default is GET

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                inputStream.bufferedReader().use {
                    it.lines().forEach { line ->
                        val list: List<HttpMessage> = mapper.readValue(line)
                        id += list.size
                        list.forEach { httpMessage -> mutableList.add("${httpMessage.user}:::${httpMessage.message}") }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("HTTPResolver", "Server doesn't exist")
        }
        return mutableList
    }

    fun sendMessage(
        community: String,
        address: String,
        pseudo: String,
        message: String,
    ) : Boolean {
        val msgAsBytes = message.toByteArray(StandardCharsets.UTF_8)
        if (msgAsBytes.size > 160) {
            Log.w("HttpSender","Message cannot be more than 35 character as UTF_8 byte array.")
            return false
        }
        val url = URL("http://chat.$address/message/$community")

        val payload = "{\"username\": \"$pseudo\", \"message\": \"$message\"}"

        var result = false
        try {
            with(url.openConnection() as HttpURLConnection) {
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Content-Length", payload.length.toString())
                doOutput = true

                val wr = OutputStreamWriter(outputStream)
                wr.write(payload)
                wr.flush()

                inputStream.bufferedReader().use {
                    it.lines().forEach { line ->
                        result = mapper.readValue(line)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("HTTPResolver", "Server doesn't exist")
        }
        return result
    }

    fun communityChecker (
        address : String,
        community: String
    ) : Boolean {
        val url = URL("http://chat.$address/community/$community")

        var result = false
        isConnected = false
        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"  // optional default is GET

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                inputStream.bufferedReader().use {
                    it.lines().forEach { line ->
                        result = mapper.readValue(line)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("HTTPResolver", "Server doesn't exist")
        }
        if(result) {
            isConnected = true
        }
        return result
    }
}