package nl.codingwithlinda.pagekeeper.core.di

import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.RoomBookRepository
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository

class AppModule {

    val bookRepository: BookRepository by lazy {
        RoomBookRepository()
    }

}