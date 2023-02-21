package fr.chatavion.client.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import fr.chatavion.client.db.entity.Community
import fr.chatavion.client.db.entity.CommunityWithMessages

@Dao
interface CommunityDAO {

    @Transaction
    @Query("SELECT * FROM community WHERE communityId = (:id)")
    fun getById(id: Int): LiveData<CommunityWithMessages>

//    @Transaction
//    @Query("SELECT * FROM community")
//    fun getAll(): LiveData<List<CommunityWithMessages>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(community: Community)

    @Delete
    suspend fun delete(community: Community)

    @Transaction
    @Query("SELECT communityId FROM community WHERE name = (:name) AND address = (:address) LIMIT 1")
    fun getId(name: String, address: String): Int
}
