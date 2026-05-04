package nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.dao.BookDao
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.model.toDomain
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.model.toEntity
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import java.io.File

internal class RoomBookRepository(
    private val dao: BookDao,
    private val filesDir: File,
) : BookRepository {

    override val books: Flow<List<Book>>
        get() = dao.getAllBooks().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getBookByISBN(ISBN: String): Book? =
        dao.getBookByIsbn(ISBN)?.toDomain()

    override suspend fun findDuplicate(isbn: String, title: String, author: String): Book? =
        dao.getBookByIsbn(isbn)?.toDomain()
            ?: dao.getBookByTitleAndAuthor(title, author)?.toDomain()

    override suspend fun upsertBook(book: Book) {
        dao.upsertBook(book.toEntity())
    }

    override fun observeBook(ISBN: String): Flow<Book?> =
        dao.observeBook(ISBN).map { it?.toDomain() }

    override suspend fun deleteBook(ISBN: String) {
        dao.deleteBook(ISBN)
        File(filesDir, "$ISBN.png").delete()
        File(filesDir, "$ISBN.fb2").delete()
        filesDir.listFiles()?.filter { it.name.startsWith(ISBN) }?.forEach { it.delete() }
    }
}