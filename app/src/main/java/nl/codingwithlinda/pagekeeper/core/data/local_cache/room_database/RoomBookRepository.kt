package nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.model.Book

internal class RoomBookRepository(private val dao: BookDao) : BookRepository {

    override val books: Flow<List<Book>>
        get() = dao.getAllBooks().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getBookByISBN(ISBN: String): Book {
        return dao.getBookByIsbn(ISBN)?.toDomain()
            ?: throw NoSuchElementException("Book with ISBN $ISBN not found")
    }

    override suspend fun upsertBook(book: Book) {
        dao.upsertBook(book.toEntity())
    }

    override suspend fun deleteBook(ISBN: String) {
        dao.deleteBook(ISBN)
    }
}