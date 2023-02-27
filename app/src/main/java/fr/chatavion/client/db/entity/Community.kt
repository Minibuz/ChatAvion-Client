package fr.chatavion.client.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "community",
    indices = [
        Index(value = ["name", "address"], unique = true)
    ]
)
data class Community(
    val name: String,
    val address: String,
    var pseudo: String,
    var idLastMessage: Int,
    @PrimaryKey(autoGenerate = true)
    val communityId: Int = 0
)

