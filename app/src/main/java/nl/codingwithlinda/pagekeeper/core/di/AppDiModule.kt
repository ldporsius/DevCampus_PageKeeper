package nl.codingwithlinda.pagekeeper.core.di

import androidx.room.Room
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.PageKeeperDatabase
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.RoomBookRepository
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation.BookDetailViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appDataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            PageKeeperDatabase::class.java,
            "pagekeeper.db"
        ).fallbackToDestructiveMigration(false).build()
    }
    single { RoomBookRepository(get<PageKeeperDatabase>().bookDao()) } bind BookRepository::class
}

val appPresentationModule = module {
    viewModelOf(::LibraryViewModel)
    factory { (isbn: String) -> BookDetailViewModel(isbn, get()) }
}