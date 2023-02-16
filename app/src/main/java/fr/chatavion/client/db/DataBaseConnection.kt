package fr.chatavion.client.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.chatavion.client.db.dao.CommunityDAO
import fr.chatavion.client.db.dao.MessageDAO
import fr.chatavion.client.db.entity.Community
import fr.chatavion.client.db.entity.Message

@Database(
    version = 1,
    entities = [Community::class, Message::class]
)
abstract class DataBaseConnection : RoomDatabase() {
    abstract fun messageDao(): MessageDAO
    abstract fun communityDao(): CommunityDAO

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: DataBaseConnection? = null

        fun getInstance(context: Context): DataBaseConnection {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): DataBaseConnection {

            return Room.databaseBuilder(
                context.applicationContext,
                DataBaseConnection::class.java, "DB"
            ).build()
        }
    }
}