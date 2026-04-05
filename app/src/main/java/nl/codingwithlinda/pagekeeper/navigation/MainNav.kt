package nl.codingwithlinda.pagekeeper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.presentation.ImportBookMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.NavigationMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.design_system.components.AppNavigation
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation.BookDetailRoot
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BooksRoot
import nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation.MultiSelectRoot
import org.koin.compose.koinInject

@Composable
fun MainNav(
    onImportBook: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val backStack = rememberNavBackStack(BookListRoute)
    val controller = koinInject<MenuActionController>()


    val selectedIndex = when (backStack.lastOrNull()) {
        is FavoritesRoute -> 1
        is FinishedRoute -> 2
        else -> 0
    }

    fun navigate(destination: Destination) {
        when(destination){
            is BookListRoute -> {
                backStack.add(BookListRoute); backStack.retainAll { it is BookListRoute }
            }
            is FavoritesRoute ->{
                backStack.add(FavoritesRoute); backStack.retainAll { it is FavoritesRoute }
            }
            is BookDetailRoute -> {
                backStack.add(BookDetailRoute(destination.ISBN))
            }
            FinishedRoute -> {
                backStack.add(FinishedRoute); backStack.retainAll { it is FinishedRoute }
            }
            is MultiSelectRoute -> {
                backStack.add(MultiSelectRoute(destination.filter))
            }
        }
    }

    ObserveAsEvents(controller.actions) { action ->
        when (action) {
            ImportBookMenuAction -> onImportBook()
            is NavigationMenuAction -> navigate(action.destination)
        }
    }

    val onLibrary = { navigate(BookListRoute) }
    val onFavorites = { navigate(FavoritesRoute) }
    val onFinished = { navigate(FinishedRoute) }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<BookListRoute> {
                NavScaffold(selectedIndex, onLibrary, onFavorites, onFinished) {
                    BooksRoot(
                        activeFilter = BookFilter.All,
                        onNavigateToDetail = { isbn -> backStack.add(BookDetailRoute(isbn)) },
                        onImportBook = onImportBook
                    )
                }
            }

            entry<FavoritesRoute> {
                NavScaffold(selectedIndex, onLibrary, onFavorites, onFinished) {
                    BooksRoot(activeFilter = BookFilter.Favorites)
                }
            }

            entry<FinishedRoute> {
                NavScaffold(selectedIndex, onLibrary, onFavorites, onFinished) {
                    BooksRoot(activeFilter = BookFilter.Finished)
                }
            }

            entry<BookDetailRoute> { key ->
                BookDetailRoot(
                    isbn = key.ISBN,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
            entry<MultiSelectRoute> { key ->
                MultiSelectRoot(
                    filter = key.filter,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

@Composable
private fun NavScaffold(
    selectedIndex: Int,
    onLibrary: () -> Unit,
    onFavorites: () -> Unit,
    onFinished: () -> Unit,
    content: @Composable () -> Unit,
) {
    AppNavigation(
        selectedIndex = selectedIndex,
        onLibrary = onLibrary,
        onFavorites = onFavorites,
        onFinished = onFinished,
        content = content,
    )
}
