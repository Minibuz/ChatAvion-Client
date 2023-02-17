package fr.chatavion.client.db.viewModel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.*
import fr.chatavion.client.db.DataBaseConnection
import fr.chatavion.client.db.dao.CommunityDAO
import fr.chatavion.client.db.entity.Community
import fr.chatavion.client.db.entity.CommunityWithMessages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommunityViewModel(application: Application) : AndroidViewModel(application) {
    val readAllData: LiveData<List<CommunityWithMessages>>
    private var communityDao: CommunityDAO =
        DataBaseConnection.getInstance(application).communityDao()

    init {
        readAllData = communityDao.getAll()
    }

    fun getAll(): LiveData<List<CommunityWithMessages>> {
        return communityDao.getAll()
    }

    fun getById(id: Int): LiveData<CommunityWithMessages> {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
        return communityDao.getById(id)
//            } catch (e: SQLiteConstraintException) {
//                Log.e("SQLEXCEPTION", e.toString())
//            }
//    }
    }

    @Throws(SQLiteConstraintException::class)
    fun insert(community: Community) {
        viewModelScope.launch(Dispatchers.IO) {
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
                return MessageViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}