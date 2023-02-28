package fr.chatavion.client.connection.http

/**
 * Represents an HTTP message consisting of an ID, a user, and a message.
 */
data class HttpMessage(val id: String, val user: String, val message: String)
