package nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.dao.BookDao
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.model.BookEntity

@Database(
    entities = [BookEntity::class],
    version = 4,
    exportSchema = false
)
internal abstract class PageKeeperDatabase : RoomDatabase() {
    internal abstract fun bookDao(): BookDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN currentSection INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}