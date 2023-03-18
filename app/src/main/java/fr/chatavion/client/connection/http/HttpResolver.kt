package fr.chatavion.client.connection.http

import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * This class represents an HTTP resolver used to interact with a chat server through HTTP requests.
 */
class HttpResolver {

    private val mapper = jacksonObjectMapper()
    var id: Int = 0
    var isConnected: Boolean = false

    /**
     * Sends a request to the server to get a list of the `nbMessage` most recent messages from the `community`.
     *
     * @param community The name of the community.
     * @param address The address of the server.
     * @param nbMessage The number of most recent messages to retrieve.
     * @return A list of strings, each string in the format `user:::message`.
     */
    fun requestHistory(
        community: String,
        address: String,
        nbMessage: Int,
    ): List<String> {
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

    /**
     * Sends a message to the `community`.
     *
     * @param community The name of the community.
     * @param address The address of the server.
     * @param pseudo The username of the user sending the message.
     * @param message The content of the message.
     * @return A boolean indicating if the message was sent successfully.
     */
    fun sendMessage(
        community: String,
        address: String,
        pseudo: String,
        message: String,
    ): Boolean {
        val msgAsBytes = message.toByteArray(StandardCharsets.UTF_8)
        if (msgAsBytes.size > 160) {
            Log.w("HttpSender", "Message cannot be more than 160 character as UTF_8 byte array.")
            return false
        }
        val url = URL("http://chat.$address/message/$community")

        val pseudoEscaped = pseudo.replace("\\", "\\\\").replace("\"", "\\\"")
        val messageEscaped = message.replace("\\", "\\\\").replace("\"", "\\\"")
        val payload = "{\"username\": \"$pseudoEscaped\", \"message\": \"$messageEscaped\"}"

        var result = false
        try {
            with(url.openConnection() as HttpURLConnection) {
                setRequestProperty("Content-Type", "application/json")
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
            Log.e("HTPPResolver", e.message.toString())
        }
        return result
    }

    /**
     * Checks if a `community` exists on the server.
     *
     * @param address The address of the server.
     * @param community The name of the community.
     * @return A boolean indicating if the community exists on the server.
     */
    fun communityChecker(
        address: String,
        community: String
    ): Boolean {
        val url = URL("http://chat.$address/community/$community")

        var result = false
        isConnected = false
        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"  // optional default is GET

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                inputStream.bufferedReader().use {
                    it.lines().forEach { line ->
                        val value = mapper.readValue<Int>(line)
                        result = value >= -1
                        Log.i("CommunityCheck", result.toString())
                        val tmp: Int = mapper.readValue(line)
                        id = if (tmp < 0) 0 else tmp
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("HTTPResolver", "Server doesn't exist")
        }
        if (result) {
            isConnected = true
        }
        return result
    }
}