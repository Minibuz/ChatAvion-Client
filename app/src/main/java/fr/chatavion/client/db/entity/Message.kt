package fr.chatavion.client.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class Message(
    val pseudo: String,
    val message: String,
    val communityId: Int,
    val status: MessageStatus,
    var send: Boolean,
    @PrimaryKey(autoGenerate = true)
    val messageId: Int = 0
)

enum class MessageStatus {
    SEND,
    RECEIVED
}
