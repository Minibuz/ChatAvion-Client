package fr.chatavion.client.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class Message(
    @PrimaryKey
    val messageId: Long,
    val pseudo: String,
    val message: String,
    val communityId: Long
)
