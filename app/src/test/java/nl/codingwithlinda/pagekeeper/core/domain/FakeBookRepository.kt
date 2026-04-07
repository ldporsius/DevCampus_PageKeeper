package nl.codingwithlinda.pagekeeper.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.model.Book

class FakeBookRepository : BookRepository {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    override val books: Flow<List<Book>> = _books.asStateFlow()

    val savedBooks: List<Book> get() = _books.value

    override suspend fun getBookByISBN(ISBN: String): Book? = _books.value.find { it.ISBN == ISBN }
    override suspend fun findDuplicate(isbn: String, title: String, author: String): Book? =
        _books.value.find { it.ISBN == isbn }
            ?: _books.value.find { it.title == title && it.author == author }
    override suspend fun upsertBook(book: Book) { _books.value = _books.value + book }
    override suspend fun deleteBook(ISBN: String) { _books.value = _books.value.filterNot { it.ISBN == ISBN } }
}