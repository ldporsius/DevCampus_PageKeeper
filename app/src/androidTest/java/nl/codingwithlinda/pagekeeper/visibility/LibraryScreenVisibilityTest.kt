package nl.codingwithlinda.pagekeeper.visibility

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListState
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryScreen
import org.junit.After
import org.junit.Rule
import org.junit.Test

class LibraryScreenVisibilityTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val sampleBook = BookUi(
        isbn = "9780743273565",
        title = "The Great Gatsby",
        author = "F. Scott Fitzgerald",
        imgUrl = "",
        formattedDate = "Apr 1, 2025"
    )

    @After
    fun resetOrientation() {
        device.setOrientationNatural()
    }

    // region Empty state

    @Test
    fun emptyState_portrait_allElementsVisible() {
        device.setOrientationNatural()
        setEmptyContent()
        assertEmptyStateVisible()
    }

    @Test
    fun emptyState_landscape_allElementsVisible() {
        device.setOrientationLandscape()
        setEmptyContent()
        assertEmptyStateVisible()
    }

    // endregion

    // region With books

    @Test
    fun withBooks_portrait_allElementsVisible() {
        device.setOrientationNatural()
        setContentWithBooks()
        assertBookItemVisible()
    }

    @Test
    fun withBooks_landscape_allElementsVisible() {
        device.setOrientationLandscape()
        setContentWithBooks()
        assertBookItemVisible()
    }

    // endregion

    // region Helpers

    private fun setEmptyContent() {
        composeRule.setContent {
            PageKeeperTheme {
                LibraryScreen(
                    state = BookListState(),
                    isImporting = false,
                    onImportBook = {},
                    onCancelImport = {},
                    onLibraryAction = {},
                    onAction = {}
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun setContentWithBooks() {
        composeRule.setContent {
            PageKeeperTheme {
                LibraryScreen(
                    state = BookListState(books = listOf(sampleBook)),
                    isImporting = false,
                    onImportBook = {},
                    onCancelImport = {},
                    onLibraryAction = {},
                    onAction = {}
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun assertEmptyStateVisible() {
        composeRule.onNodeWithText("Your library is empty").assertIsDisplayed()
        composeRule.onNodeWithText("Import your first book to start building your library").assertIsDisplayed()
        composeRule.onNodeWithText("Import Book").assertIsDisplayed()
    }

    private fun assertBookItemVisible() {
        composeRule.onNodeWithText("The Great Gatsby").assertIsDisplayed()
        composeRule.onNodeWithText("F. Scott Fitzgerald").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Favourite").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Mark as finished").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Share").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Delete").assertIsDisplayed()
    }

    // endregion
}
