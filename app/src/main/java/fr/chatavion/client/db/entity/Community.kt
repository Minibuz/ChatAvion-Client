package fr.chatavion.client.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community")
data class Community(
    @PrimaryKey
    val communityId: Long,
    val name: String,
    val address: String,
    val Pseudo: String
)

