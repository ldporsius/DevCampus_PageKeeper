package nl.codingwithlinda.pagekeeper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation.BookDetailRoot
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BooksRoot

@Composable
fun MainNav(
    onImportBook: () -> Unit,
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(BookListRoute)
) {

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<BookListRoute> {
                BooksRoot(
                    activeFilter = BookFilter.All,
                    onNavigateToDetail = { isbn -> backStack.add(BookDetailRoute(isbn)) },
                    onImportBook = onImportBook
                )
            }

            entry<BookDetailRoute> { key ->
                BookDetailRoot(
                    isbn = key.ISBN,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<FavoritesRoute> {
                BooksRoot(activeFilter = BookFilter.Favorites)
            }

            entry<FinishedRoute> {
                BooksRoot(activeFilter = BookFilter.Finished)
            }
        }
    )

}
