package fr.chatavion.client.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    version = 1,
//    entities = [Plant::class]
)

//@TypeConverters(Converters::class)
abstract class DataBaseConnection : RoomDatabase() {
//    abstract fun plantDao(): PlantDAO

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