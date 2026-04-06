package nl.codingwithlinda.pagekeeper.visibility

import android.content.res.Resources
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import nl.codingwithlinda.pagekeeper.core.domain.FakeBookParser
import nl.codingwithlinda.pagekeeper.core.domain.FakeBookRepository
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookFormatValidator
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import nl.codingwithlinda.pagekeeper.core.presentation.DefaultMenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.MenuActionController
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation.BookDetailViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.search.SearchViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation.MultiSelectViewModel
import nl.codingwithlinda.pagekeeper.navigation.MainNav
import org.junit.After
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Tests that navigation chrome elements are visible in each form-factor layout:
 *
 *   Phone portrait  → AppNavDrawer (hamburger menu + title bar)
 *   Phone landscape → AppNavRail   (side rail with nav items)
 *   Tablet          → AppNavRail   (always, regardless of orientation)
 *
 * Run this test class on both a phone emulator and a tablet emulator to cover
 * all four form-factor/orientation combinations. Each device runs 2 tests and
 * skips 2 (the ones that don't apply to its form factor).
 */
class NavigationChromeVisibilityTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val isPhone
        get() = Resources.getSystem().configuration.smallestScreenWidthDp < 600

    private val testBook = Book(
        ISBN = "9780743273565",
        title = "The Great Gatsby",
        author = "F. Scott Fitzgerald",
        imgUrl = "",
        dateCreated = 1_743_465_600_000L
    )

    @Before
    fun setUp() {
        stopKoin()
        startKoin {
            androidContext(InstrumentationRegistry.getInstrumentation().targetContext)
            modules(module {
                single<BookRepository> { FakeBookRepository(listOf(testBook)) } bind BookRepository::class
                single<BookParser> { FakeBookParser() } bind BookParser::class
                single<BookFormatValidator> {
                    object : BookFormatValidator {
                        override fun isSupportedFormat(uri: String) = true
                    }
                } bind BookFormatValidator::class
                single<MenuActionController> { DefaultMenuActionController() }
                viewModelOf(::LibraryViewModel)
                viewModelOf(::BookListViewModel)
                viewModelOf(::MultiSelectViewModel)
                viewModelOf(::SearchViewModel)
                viewModel { (isbn: String) -> BookDetailViewModel(isbn, get()) }
            })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        device.setOrientationNatural()
    }

    // region Phone tests

    /**
     * Phone portrait: AppNavDrawer is used.
     * Expects a hamburger menu button, the screen title, and the search button.
     */
    @Test
    fun phonePortrait_drawerLayout_chromeElementsVisible() {
        Assume.assumeTrue("Drawer layout only applies to phones — skipping on tablet", isPhone)
        device.setOrientationNatural()
        setContent()

        composeRule.onNodeWithContentDescription("Open menu").assertIsDisplayed()
        // "Library" appears twice: top bar title + closed drawer item still in the tree
        composeRule.onAllNodesWithText("Library").assertCountEquals(2)
        composeRule.onNodeWithContentDescription("Search").assertIsDisplayed()
        composeRule.onNodeWithText("The Great Gatsby").assertIsDisplayed()
    }

    /**
     * Phone landscape: AppNavRail is used.
     * Expects rail items with labels and the import button.
     */
    @Test
    fun phoneLandscape_railLayout_chromeElementsVisible() {
        Assume.assumeTrue("Phone landscape rail test — skipping on tablet", isPhone)
        device.setOrientationLandscape()
        setContent()

        assertRailChromeVisible()
    }

    // endregion

    // region Tablet tests

    /**
     * Tablet portrait: AppNavRail is always used on tablets.
     * Expects rail items with labels and the import button.
     */
    @Test
    fun tabletPortrait_railLayout_chromeElementsVisible() {
        Assume.assumeTrue("Rail-always layout only applies to tablets — skipping on phone", !isPhone)
        device.setOrientationNatural()
        setContent()

        assertRailChromeVisible()
    }

    /**
     * Tablet landscape: AppNavRail is always used on tablets.
     * Expects rail items with labels and the import button.
     */
    @Test
    fun tabletLandscape_railLayout_chromeElementsVisible() {
        Assume.assumeTrue("Rail-always layout only applies to tablets — skipping on phone", !isPhone)
        device.setOrientationLandscape()
        setContent()

        assertRailChromeVisible()
    }

    // endregion

    // region Helpers

    private fun assertRailChromeVisible() {
        composeRule.onNodeWithText("Library").assertIsDisplayed()
        composeRule.onNodeWithText("Favorites").assertIsDisplayed()
        composeRule.onNodeWithText("Finished").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Import book").assertIsDisplayed()
        composeRule.onNodeWithText("The Great Gatsby").assertIsDisplayed()
    }

    private fun setContent() {
        composeRule.setContent {
            PageKeeperTheme {
                MainNav(onImportBook = {})
            }
        }
        composeRule.waitForIdle()
    }

    // endregion
}
