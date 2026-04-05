package nl.codingwithlinda.pagekeeper.visibility

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListState
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.FavoritesScreen
import org.junit.After
import org.junit.Rule
import org.junit.Test

class FavoritesScreenVisibilityTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val favoriteBook = BookUi(
        isbn = "isbn_fav",
        title = "Pride and Prejudice",
        author = "Jane Austen",
        imgUrl = "",
        formattedDate = "Jan 1, 2025",
        isFavorite = true
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

    // region With favorites

    @Test
    fun withFavorites_portrait_allElementsVisible() {
        device.setOrientationNatural()
        setContentWithBooks()
        assertBookItemVisible()
    }

    @Test
    fun withFavorites_landscape_allElementsVisible() {
        device.setOrientationLandscape()
        setContentWithBooks()
        assertBookItemVisible()
    }

    // endregion

    // region Helpers

    private fun setEmptyContent() {
        composeRule.setContent {
            PageKeeperTheme {
                FavoritesScreen(state = BookListState(), onAction = {})
            }
        }
        composeRule.waitForIdle()
    }

    private fun setContentWithBooks() {
        composeRule.setContent {
            PageKeeperTheme {
                FavoritesScreen(
                    state = BookListState(books = listOf(favoriteBook)),
                    onAction = {}
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun assertEmptyStateVisible() {
        composeRule.onNodeWithText("No favorites yet").assertIsDisplayed()
        composeRule.onNodeWithText("Mark a book as favourite to find it here").assertIsDisplayed()
    }

    private fun assertBookItemVisible() {
        composeRule.onNodeWithText("Pride and Prejudice").assertIsDisplayed()
        composeRule.onNodeWithText("Jane Austen").assertIsDisplayed()
    }

    // endregion
}
