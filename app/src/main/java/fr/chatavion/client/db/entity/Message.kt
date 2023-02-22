package fr.chatavion.client.db.entity

data class Message(
    val pseudo: String,
    val message: String,
    val status: MessageStatus,
    var send: Boolean,
)

enum class MessageStatus {
    SEND,
    RECEIVED
}
