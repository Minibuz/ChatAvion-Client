package fr.chatavion.client.db.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.room.*
import fr.chatavion.client.db.entity.Community
import fr.chatavion.client.db.entity.CommunityWithMessages

@Dao
interface CommunityDAO {

    @Transaction
    @Query("SELECT * FROM community WHERE communityId = (:communityID);")
    fun getById(communityID: Int): LiveData<CommunityWithMessages>

    @Transaction
    @Query("SELECT * FROM community;")
    fun getAll(): LiveData<List<CommunityWithMessages>>

    @Insert
    @Throws(SQLiteConstraintException::class)
    suspend fun insert(community: Community)

    @Delete
    suspend fun delete(community: Community)
}
