package nl.codingwithlinda.pagekeeper.core.domain.local_cache

import kotlinx.coroutines.flow.Flow
import nl.codingwithlinda.pagekeeper.core.domain.model.Book

interface BookRepository {

    val books: Flow<List<Book>>

    suspend fun getBookByISBN(ISBN: String): Book
}