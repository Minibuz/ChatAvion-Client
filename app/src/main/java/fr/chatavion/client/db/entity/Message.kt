package fr.chatavion.client.db.entity

/**
A data class representing a message sent or received in a chat application.
- @property pseudo the pseudonym of the sender or receiver of the message.
- @property message the content of the message.
- @property status the status of the message, indicating whether it has been sent or received.
- @property send a flag indicating whether the message has been sent by the user or not.
 */
data class Message(
    val pseudo: String,
    val message: String,
    val status: MessageStatus,
    var send: Boolean,
    var sendRetry : Int,
)

/**
An enum class representing the status of a [Message].
@property SEND the status indicating that the message has been sent.
@property RECEIVED the status indicating that the message has been received.
 */
enum class MessageStatus {
    SEND,
    RECEIVED
}
