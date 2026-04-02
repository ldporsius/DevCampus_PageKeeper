package nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BookEntity::class],
    version = 2,
    exportSchema = false
)
internal abstract class PageKeeperDatabase : RoomDatabase() {
    internal abstract fun bookDao(): BookDao
}