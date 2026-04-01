package nl.codingwithlinda.pagekeeper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation.BookDetailRoot
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryRoot

@Composable
fun MainNav(
    modifier: Modifier = Modifier,
    bookRepository: BookRepository
) {

    val backStack = rememberNavBackStack(BookListRoute)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<BookListRoute> {
                LibraryRoot(bookRepository)
            }

            entry<BookDetailRoute> {
                BookDetailRoot()
            }
        }
    )
}