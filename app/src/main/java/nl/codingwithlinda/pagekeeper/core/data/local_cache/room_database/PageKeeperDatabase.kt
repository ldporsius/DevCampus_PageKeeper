package nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BookEntity::class],
    version = 1,
    exportSchema = false
)
internal abstract class PageKeeperDatabase : RoomDatabase() {

    internal abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var instance: PageKeeperDatabase? = null

        internal fun getInstance(context: Context): PageKeeperDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PageKeeperDatabase::class.java,
                    "pagekeeper.db"
                ).build().also { instance = it }
            }
    }
}