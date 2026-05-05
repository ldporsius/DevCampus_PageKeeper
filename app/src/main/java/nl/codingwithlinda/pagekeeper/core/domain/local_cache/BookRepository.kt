package nl.codingwithlinda.pagekeeper.core.domain.local_cache

import kotlinx.coroutines.flow.Flow
import nl.codingwithlinda.pagekeeper.core.domain.model.Book

interface BookRepository {

    val books: Flow<List<Book>>

    suspend fun getBookByISBN(ISBN: String): Book?

    suspend fun findDuplicate(isbn: String, title: String, author: String): Book?

    suspend fun upsertBook(book: Book)

    fun observeBook(ISBN: String): Flow<Book?>

    suspend fun deleteBook(ISBN: String)

    suspend fun updateLastOpenedDate(isbn: String, date: Long)
}