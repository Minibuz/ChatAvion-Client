package fr.chatavion.client.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fr.chatavion.client.db.entity.Message

@Dao
interface MessageDAO {
    @Insert
    fun insertAll(vararg messages: Message)

    @Query("SELECT * FROM message")
    fun getAll(): List<Message>
}
