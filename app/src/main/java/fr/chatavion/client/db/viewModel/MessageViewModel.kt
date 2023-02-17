package fr.chatavion.client.db.viewModel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.*
import fr.chatavion.client.db.DataBaseConnection
import fr.chatavion.client.db.dao.MessageDAO
import fr.chatavion.client.db.entity.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageViewModel(application: Application) : AndroidViewModel(application) {
    private val readAllData: LiveData<List<Message>>
    private var messageDao: MessageDAO = DataBaseConnection.getInstance(application).messageDao()

    init {
        readAllData = messageDao.getAll()
    }

    fun getAll(): LiveData<List<Message>> {
        return messageDao.getAll()
    }

    @Throws(SQLiteConstraintException::class)
    fun insertAll(vararg messages: Message) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageDao.insertAll(*messages)
            } catch (e: SQLiteConstraintException) {
                Log.e("SQLEXCEPTION", e.toString())
            }
        }
    }

    class MessageFactory(
        private val application: Application,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
                return MessageViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}