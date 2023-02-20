package fr.chatavion.client.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community")
data class Community(
    val name: String,
    val address: String,
    val Pseudo: String,
    @PrimaryKey(autoGenerate = true)
    val communityId: Int = 0
)

