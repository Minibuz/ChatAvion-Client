package fr.chatavion.client.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import fr.chatavion.client.db.entity.Community
import fr.chatavion.client.db.entity.CommunityWithMessages

@Dao
interface CommunityDAO {

    @Transaction
    @Query("SELECT * FROM community WHERE communityId = :communityID")
    fun getCommunityWithMessages(communityID: Int): LiveData<CommunityWithMessages>

    @Transaction
    @Query("SELECT * FROM community")
    fun getAll(): List<CommunityWithMessages>

    @Insert
    fun insert(community: Community)
}
