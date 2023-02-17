package fr.chatavion.client.db.viewModel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
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
}