package nl.codingwithlinda.pagekeeper.core.di

import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.PageKeeperDatabase
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.RoomBookRepository
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appDataModule = module {
    single { PageKeeperDatabase.getInstance(androidContext()) }
    single { RoomBookRepository(get<PageKeeperDatabase>().bookDao()) } bind BookRepository::class
}

val appPresentationModule = module {
    viewModelOf(::LibraryViewModel)
}