package fr.chatavion.client.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.chatavion.client.db.dao.CommunityDAO
import fr.chatavion.client.db.entity.Community

/**
 * A Room Database that serves as the main access point for the application's local database.
 * Provides a singleton instance of [DataBaseConnection] to access the application's database.
 *
 * @constructor Creates an instance of [DataBaseConnection]
 */
@Database(
    version = 1,
    entities = [Community::class]
)
abstract class DataBaseConnection : RoomDatabase() {

    /**
     * Provides an abstract function to access the [CommunityDAO] interface.
     *
     * @return An instance of [CommunityDAO]
     */
    abstract fun communityDao(): CommunityDAO

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: DataBaseConnection? = null

        /**
         * Returns the singleton instance of [DataBaseConnection]. If the instance doesn't exist,
         * creates a new instance of [DataBaseConnection].
         *
         * @param context The application context
         * @return The singleton instance of [DataBaseConnection]
         */
        fun getInstance(context: Context): DataBaseConnection {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        /**
         * Builds a new instance of [DataBaseConnection] with the given [context] and database name "DB".
         *
         * @param context The application context
         * @return A new instance of [DataBaseConnection]
         */
        private fun buildDatabase(context: Context): DataBaseConnection {
            return Room.databaseBuilder(
                context.applicationContext,
                DataBaseConnection::class.java, "DB"
            ).build()
        }
    }
}
