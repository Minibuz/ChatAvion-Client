package fr.chatavion.client.connection

data class Message(
    val status: MessageStatus,
    val user: String,
    val message: String,
    var send: Boolean,
)

enum class MessageStatus {
    SEND,
    RECEIVED
}
