package fr.chatavion.client.db.entity

import androidx.room.Entity

@Entity(tableName = "community", primaryKeys = ["name", "address"])
data class Community(
    val name: String,
    val address: String,
    val pseudo: String
)
