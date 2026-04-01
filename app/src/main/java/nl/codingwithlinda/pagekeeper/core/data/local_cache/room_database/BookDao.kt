package nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface BookDao {

    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isbn = :isbn")
    suspend fun getBookByIsbn(isbn: String): BookEntity?

    @Upsert
    suspend fun upsertBook(book: BookEntity)

    @Query("DELETE FROM books WHERE isbn = :isbn")
    suspend fun deleteBook(isbn: String)
}