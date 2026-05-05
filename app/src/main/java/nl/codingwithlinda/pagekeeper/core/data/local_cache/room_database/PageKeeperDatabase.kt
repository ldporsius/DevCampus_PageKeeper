package nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.dao.BookDao
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.model.BookEntity

@Database(
    entities = [BookEntity::class],
    version = 7,
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
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN currentSectionOffset INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN lastOpenedDate INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE books_new (
                        isbn TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        author TEXT NOT NULL,
                        imgUrl TEXT NOT NULL,
                        dateCreated INTEGER NOT NULL,
                        isFavorite INTEGER NOT NULL DEFAULT 0,
                        isFinished INTEGER NOT NULL DEFAULT 0,
                        currentSection INTEGER NOT NULL DEFAULT 0,
                        currentElementId INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO books_new (isbn, title, author, imgUrl, dateCreated, isFavorite, isFinished, currentSection, currentElementId)
                    SELECT isbn, title, author, imgUrl, dateCreated, isFavorite, isFinished, currentSection, 0
                    FROM books
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE books")
                db.execSQL("ALTER TABLE books_new RENAME TO books")
            }
        }
    }
}