package nl.codingwithlinda.pagekeeper.core.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.PageKeeperDatabase
import nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.RoomBookRepository
import nl.codingwithlinda.pagekeeper.core.data.remote.ContentResolverBookFormatValidator
import nl.codingwithlinda.pagekeeper.core.data.remote.FN2BookPager
import nl.codingwithlinda.pagekeeper.core.data.remote.FN2BookParser
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookFormatValidator
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookPager
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import nl.codingwithlinda.pagekeeper.core.presentation.DefaultMenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.MenuActionController
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation.BookDetailViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.search.SearchViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation.MultiSelectViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module


private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE books ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE books ADD COLUMN isFinished INTEGER NOT NULL DEFAULT 0")
    }
}

val appDataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            PageKeeperDatabase::class.java,
            "pagekeeper.db"
        ).addMigrations(MIGRATION_2_3).fallbackToDestructiveMigration(false).build()
    }
    single { RoomBookRepository(get<PageKeeperDatabase>().bookDao(), androidContext().filesDir) } bind BookRepository::class
    single { FN2BookParser(androidContext()) } bind BookParser::class
    single { FN2BookPager(androidContext()) } bind BookPager::class
    single { ContentResolverBookFormatValidator(androidContext()) } bind BookFormatValidator::class
    single<MenuActionController> { DefaultMenuActionController() }
}

val appPresentationModule = module {
    viewModel(qualifier = named("all")) { BookListViewModel(get(), get(), get(), BookFilter.All) }
    viewModel(qualifier = named("favorites")) { BookListViewModel(get(), get(), get(), BookFilter.Favorites) }
    viewModel(qualifier = named("finished")) { BookListViewModel(get(), get(), get(), BookFilter.Finished) }
    viewModel(qualifier = named("search")) { BookListViewModel(get(), get(), get(), BookFilter.All) }
    viewModelOf(::LibraryViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::MultiSelectViewModel)
    viewModel { (isbn: String) -> BookDetailViewModel(isbn, get(), get()) }
}
