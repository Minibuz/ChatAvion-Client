package fr.chatavion.client.db.entity

/**
 * A data class representing a message sent or received in a chat application.
 */
data class Message(
    val pseudo: String,
    val message: String,
    val status: MessageStatus,
    var send: Boolean,
    var sendRetry: Int,
)

/**
 * An enum class representing the status of a [Message].
 */
enum class MessageStatus {
    SEND,
    RECEIVED
}
