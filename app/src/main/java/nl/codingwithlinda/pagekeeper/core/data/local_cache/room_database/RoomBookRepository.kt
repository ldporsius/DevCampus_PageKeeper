package nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database

import kotlinx.coroutines.flow.Flow
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.model.Book

class RoomBookRepository: BookRepository {


    override val books: Flow<List<Book>>
        get() = TODO("Not yet implemented")

    override suspend fun getBookByISBN(ISBN: String): Book {
        TODO("Not yet implemented")
    }
}