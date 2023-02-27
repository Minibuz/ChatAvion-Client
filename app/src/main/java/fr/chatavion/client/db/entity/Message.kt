package fr.chatavion.client.db.entity

data class Message(
    val pseudo: String,
    val message: String,
    val status: MessageStatus,
    var times: Int,
    var send: Boolean,
    var sendRetry : Int,
)

enum class MessageStatus {
    SEND,
    RECEIVED
}
