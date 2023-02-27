package fr.chatavion.client.db.viewModel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.*
import fr.chatavion.client.db.DataBaseConnection
import fr.chatavion.client.db.dao.CommunityDAO
import fr.chatavion.client.db.entity.Community
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

/**
 * View Model class for managing Community entities in the database.
 *
 * @param application The application instance.
 */
class CommunityViewModel(application: Application) : AndroidViewModel(application) {

    // The DAO instance for Community entities.
    private var communityDao: CommunityDAO =
        DataBaseConnection.getInstance(application).communityDao()

    /**
     * Returns the ID of the Community entity with the given name and address.
     *
     * @param name The name of the Community entity.
     * @param address The address of the Community entity.
     * @return The ID of the Community entity.
     */
    fun getId(name: String, address: String): Int {
        return communityDao.getId(name, address)
    }

    /**
     * Returns a LiveData object containing the Community entity with the given ID.
     *
     * @param id The ID of the Community entity.
     * @return A LiveData object containing the Community entity.
     */
    fun getById(id: Int): LiveData<Community> {
        return communityDao.getById(id)
    }

    /**
     * Returns a LiveData object containing all the Community entities in the database.
     *
     * @return A LiveData object containing a List of Community entities.
     */
    fun getAll(): LiveData<List<Community>> {
        return communityDao.getAll()
    }

    /**
     * Deletes the given Community entity from the database.
     *
     * @param community The Community entity to be deleted.
     */
    fun delete(community: Community) {
        viewModelScope.launch {
            communityDao.delete(community)
        }
    }

    /**
     * Inserts the given Community entity into the database.
     *
     * @param community The Community entity to be inserted.
     * @throws SQLiteConstraintException if there's a constraint violation.
     */
    @Throws(SQLiteConstraintException::class)
    fun insert(community: Community) {
        viewModelScope.launch(IO) {
            try {
                communityDao.insert(community)
            } catch (e: SQLiteConstraintException) {
                Log.e("SQLEXCEPTION", e.toString())
            }
        }
    }

    /**
     * Factory class for creating CommunityViewModel instances.
     *
     * @param application The application instance.
     */
    class CommunityFactory(
        private val application: Application,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
                return CommunityViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
