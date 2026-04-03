package nl.codingwithlinda.pagekeeper.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import nl.codingwithlinda.pagekeeper.core.domain.FakeBookParser
import nl.codingwithlinda.pagekeeper.core.domain.FakeBookRepository
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation.BookDetailViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

class NavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val testBook = Book(
        ISBN = "9780743273565",
        title = "The Great Gatsby",
        author = "F. Scott Fitzgerald",
        imgUrl = "",
        dateCreated = 1_743_465_600_000L // 2025-04-01
    )

    @Before
    fun setUp() {
        stopKoin()
        startKoin {
            androidContext(InstrumentationRegistry.getInstrumentation().targetContext)
            modules(
                module {
                    single<BookRepository> { FakeBookRepository(listOf(testBook)) } bind BookRepository::class
                    single<BookParser> { FakeBookParser() } bind BookParser::class
                    viewModelOf(::LibraryViewModel)
                    viewModel(qualifier = named(BookFilter.All)) { BookListViewModel(get(), BookFilter.All) }
                    viewModel { (isbn: String) -> BookDetailViewModel(isbn, get()) }
                }
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun tappingBookRow_navigatesToDetail_backButton_returnsToList() {
        composeRule.setContent {
            PageKeeperTheme {
                MainNav(
                    onImportBook = {}
                )
            }
        }

        // Library screen: book title is visible
        composeRule
            .onNodeWithText("The Great Gatsby")
            .assertIsDisplayed()

        // Tap the book row → triggers OnBookClick → NavigateToDetail event → detail pushed
        composeRule
            .onNodeWithText("The Great Gatsby")
            .performClick()

        composeRule.waitForIdle()

        // Detail screen is now showing
        composeRule
            .onNodeWithTag("book_detail_screen")
            .assertIsDisplayed()

        // Library screen is no longer in composition
        composeRule
            .onNodeWithText("The Great Gatsby")
            .assertIsNotDisplayed()

        // Press the hardware back button
        device.pressBack()
        composeRule.waitForIdle()

        // Back on library screen — book title is visible again
        composeRule
            .onNodeWithText("The Great Gatsby")
            .assertIsDisplayed()

        // Detail screen is no longer in composition
        composeRule
            .onNodeWithTag("book_detail_screen")
            .assertIsNotDisplayed()
    }
}
