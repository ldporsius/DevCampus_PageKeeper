package nl.codingwithlinda.pagekeeper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.presentation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.NavigationMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.design_system.components.AppNavigation
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation.BookDetailRoot
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BooksRoot
import org.koin.compose.koinInject

@Composable
fun MainNav(
    onImportBook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val backStack = rememberNavBackStack(BookListRoute)
    val controller = koinInject<MenuActionController>()

    ObserveAsEvents(controller.actions) {
        scope.launch {
            it.undo()
            it.execute()
        }
    }

    val selectedIndex = when (backStack.lastOrNull()) {
        is FavoritesRoute -> 1
        is FinishedRoute -> 2
        else -> 0
    }

    fun navigate(destination: NavKey, navigate: () -> Unit) {
        scope.launch { controller.onAction(NavigationMenuAction(destination, navigate)) }
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<BookListRoute> {
                AppNavigation(
                    selectedIndex = selectedIndex,
                    onLibrary = { navigate(BookListRoute) { backStack.add(BookListRoute); backStack.retainAll { it is BookListRoute } } },
                    onFavorites = { navigate(FavoritesRoute) { backStack.add(FavoritesRoute); backStack.retainAll { it is FavoritesRoute } } },
                    onFinished = { navigate(FinishedRoute) { backStack.add(FinishedRoute); backStack.retainAll { it is FinishedRoute } } },
                    onImportBook = onImportBook,
                ) {
                    BooksRoot(
                        activeFilter = BookFilter.All,
                        onNavigateToDetail = { isbn -> backStack.add(BookDetailRoute(isbn)) },
                        onImportBook = onImportBook
                    )
                }
            }

            entry<FavoritesRoute> {
                AppNavigation(
                    selectedIndex = selectedIndex,
                    onLibrary = { navigate(BookListRoute) { backStack.add(BookListRoute); backStack.retainAll { it is BookListRoute } } },
                    onFavorites = { navigate(FavoritesRoute) { backStack.add(FavoritesRoute); backStack.retainAll { it is FavoritesRoute } } },
                    onFinished = { navigate(FinishedRoute) { backStack.add(FinishedRoute); backStack.retainAll { it is FinishedRoute } } },
                    onImportBook = onImportBook,
                ) {
                    BooksRoot(activeFilter = BookFilter.Favorites)
                }
            }

            entry<FinishedRoute> {
                AppNavigation(
                    selectedIndex = selectedIndex,
                    onLibrary = { navigate(BookListRoute) { backStack.add(BookListRoute); backStack.retainAll { it is BookListRoute } } },
                    onFavorites = { navigate(FavoritesRoute) { backStack.add(FavoritesRoute); backStack.retainAll { it is FavoritesRoute } } },
                    onFinished = { navigate(FinishedRoute) { backStack.add(FinishedRoute); backStack.retainAll { it is FinishedRoute } } },
                    onImportBook = onImportBook,
                ) {
                    BooksRoot(activeFilter = BookFilter.Finished)
                }
            }

            entry<BookDetailRoute> { key ->
                BookDetailRoot(
                    isbn = key.ISBN,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
