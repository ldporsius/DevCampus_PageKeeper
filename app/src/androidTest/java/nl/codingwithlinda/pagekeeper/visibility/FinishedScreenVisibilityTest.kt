package nl.codingwithlinda.pagekeeper.visibility

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListState
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.FinishedScreen
import org.junit.After
import org.junit.Rule
import org.junit.Test

class FinishedScreenVisibilityTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val finishedBook = BookUi(
        isbn = "isbn_fin",
        title = "Moby Dick",
        author = "Herman Melville",
        imgUrl = "",
        formattedDate = "Feb 1, 2025",
        isFinished = true
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

    // region With finished books

    @Test
    fun withFinished_portrait_allElementsVisible() {
        device.setOrientationNatural()
        setContentWithBooks()
        assertBookItemVisible()
    }

    @Test
    fun withFinished_landscape_allElementsVisible() {
        device.setOrientationLandscape()
        setContentWithBooks()
        assertBookItemVisible()
    }

    // endregion

    // region Helpers

    private fun setEmptyContent() {
        composeRule.setContent {
            PageKeeperTheme {
                FinishedScreen(state = BookListState(), onAction = {})
            }
        }
        composeRule.waitForIdle()
    }

    private fun setContentWithBooks() {
        composeRule.setContent {
            PageKeeperTheme {
                FinishedScreen(
                    state = BookListState(books = listOf(finishedBook)),
                    onAction = {}
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun assertEmptyStateVisible() {
        composeRule.onNodeWithText("No finished books yet").assertIsDisplayed()
        composeRule.onNodeWithText("Mark a book as finished to see it here").assertIsDisplayed()
    }

    private fun assertBookItemVisible() {
        composeRule.onNodeWithText("Moby Dick").assertIsDisplayed()
        composeRule.onNodeWithText("Herman Melville").assertIsDisplayed()
    }

    // endregion
}
