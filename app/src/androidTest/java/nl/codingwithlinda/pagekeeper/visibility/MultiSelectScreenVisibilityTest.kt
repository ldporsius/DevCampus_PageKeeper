package nl.codingwithlinda.pagekeeper.visibility

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi
import nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation.MultiSelectScreen
import nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation.MultiSelectState
import org.junit.After
import org.junit.Rule
import org.junit.Test

class MultiSelectScreenVisibilityTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val books = listOf(
        BookUi(isbn = "isbn_1", title = "The Great Gatsby", author = "F. Scott Fitzgerald", imgUrl = "", formattedDate = ""),
        BookUi(isbn = "isbn_2", title = "1984", author = "George Orwell", imgUrl = "", formattedDate = "")
    )

    @After
    fun resetOrientation() {
        device.setOrientationNatural()
    }

    // region No selection

    @Test
    fun noSelection_portrait_topBarAndListVisible() {
        device.setOrientationNatural()
        setContent(selectedIsbn = emptySet())
        assertTopBarVisible(selectedCount = 0)
        assertBooksVisible()
    }

    @Test
    fun noSelection_landscape_topBarAndListVisible() {
        device.setOrientationLandscape()
        setContent(selectedIsbn = emptySet())
        assertTopBarVisible(selectedCount = 0)
        assertBooksVisible()
    }

    // endregion

    // region With selection

    @Test
    fun withSelection_portrait_countAndCheckboxesVisible() {
        device.setOrientationNatural()
        setContent(selectedIsbn = setOf("isbn_1", "isbn_2"))
        assertTopBarVisible(selectedCount = 2)
        composeRule.onAllNodesWithContentDescription("Selected").assertCountEquals(2)
    }

    @Test
    fun withSelection_landscape_countAndCheckboxesVisible() {
        device.setOrientationLandscape()
        setContent(selectedIsbn = setOf("isbn_1", "isbn_2"))
        assertTopBarVisible(selectedCount = 2)
        composeRule.onAllNodesWithContentDescription("Selected").assertCountEquals(2)
    }

    // endregion

    // region Helpers

    private fun setContent(selectedIsbn: Set<String>) {
        composeRule.setContent {
            PageKeeperTheme {
                MultiSelectScreen(
                    state = MultiSelectState(books = books, selectedIsbn = selectedIsbn),
                    onAction = {},
                    onBookAction = {}
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun assertTopBarVisible(selectedCount: Int) {
        composeRule.onNodeWithContentDescription("Navigate back").assertIsDisplayed()
        composeRule.onNodeWithText("$selectedCount selected").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Mark as favorites").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Share selected").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Delete selected").assertIsDisplayed()
    }

    private fun assertBooksVisible() {
        composeRule.onNodeWithText("The Great Gatsby").assertIsDisplayed()
        composeRule.onNodeWithText("1984").assertIsDisplayed()
    }

    // endregion
}
