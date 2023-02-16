package fr.chatavion.client.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import fr.chatavion.client.db.entity.CommunityWithMessages

@Dao
interface CommunityDAO {
    @Transaction
    @Query("SELECT * FROM Community")
    fun getCommunityWithMessages(): List<CommunityWithMessages>
}
