package nl.codingwithlinda.pagekeeper.core.di

import android.content.Context
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.PageKeeperDatabase
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.RoomBookRepository
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository

class AppModule(
    context: Context
) : IAppModule {

    private val database = PageKeeperDatabase.getInstance(context.applicationContext)

    override val bookRepository: BookRepository by lazy {
        RoomBookRepository(database.bookDao())
    }

}