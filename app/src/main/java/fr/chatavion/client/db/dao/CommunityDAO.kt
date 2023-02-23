package fr.chatavion.client.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import fr.chatavion.client.db.entity.Community

@Dao
interface CommunityDAO {

    @Transaction
    @Query("SELECT * FROM community WHERE communityId = (:id)")
    fun getById(id: Int): LiveData<Community>

    @Transaction
    @Query("SELECT * FROM community")
    fun getAll(): LiveData<List<Community>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(community: Community)

    @Delete
    suspend fun delete(community: Community)

    @Transaction
    @Query("SELECT communityId FROM community WHERE name = (:name) AND address = (:address)")
    fun getId(name: String, address: String): Int
}
