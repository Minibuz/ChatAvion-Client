package fr.chatavion.client

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fr.chatavion.client.db.DataBaseConnection
import fr.chatavion.client.db.dao.CommunityDAO
import fr.chatavion.client.db.dao.MessageDAO
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

//@RunWith(AndroidJUnit4::class)
class DataBaseTest {
    private lateinit var communityDao: CommunityDAO
    private lateinit var messageDao: MessageDAO
    private lateinit var db: DataBaseConnection

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, DataBaseConnection::class.java
        ).build()
        communityDao = db.communityDao()
        messageDao = db.messageDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeMessageAndReadInList() {

    }
}