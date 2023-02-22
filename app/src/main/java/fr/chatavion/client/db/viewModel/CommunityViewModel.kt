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

class CommunityViewModel(application: Application) : AndroidViewModel(application) {
    private var communityDao: CommunityDAO =
        DataBaseConnection.getInstance(application).communityDao()

    fun getId(name: String, address: String): Int {
        return communityDao.getId(name, address)
    }

    fun getById(id: Int): LiveData<Community> {
        return communityDao.getById(id)
    }

    fun getAll(): LiveData<List<Community>> {
        return communityDao.getAll()
    }

    fun delete(community: Community) {
        viewModelScope.launch {
            communityDao.delete(community)
        }
    }

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