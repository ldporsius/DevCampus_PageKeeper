package nl.codingwithlinda.pagekeeper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import nl.codingwithlinda.pagekeeper.core.presentation.ImportBookMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.NavigationMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.design_system.components.AppNavigation
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation.BookDetailRoot
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BooksRoot
import nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation.MultiSelectRoot
import nl.codingwithlinda.pagekeeper.feature_books.search.width_compact.SearchRoot
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

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

            is FinishedRoute -> {
                backStack.add(FinishedRoute); backStack.retainAll { it is FinishedRoute }
            }
            is SearchRoute -> {
                backStack.add(SearchRoute(destination.filter))
            }

            is MultiSelectRoute -> {
                backStack.add(MultiSelectRoute(destination.filter))
            }

            is BookDetailRoute -> {
            backStack.add(BookDetailRoute(destination.ISBN))
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
    fun onSearch(filter: BookFilter) { navigate(SearchRoute(filter)) }


    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<BookListRoute> {
                NavScaffold(selectedIndex, onLibrary, onFavorites, onFinished,
                    { onSearch(BookFilter.All) }) {
                    BooksRoot(
                        bookListViewModel = koinViewModel(qualifier = named("all")),
                        onNavigateToDetail = { isbn -> backStack.add(BookDetailRoute(isbn)) },
                        onImportBook = onImportBook
                    )
                }
            }

            entry<FavoritesRoute> {
                NavScaffold(selectedIndex, onLibrary, onFavorites, onFinished,
                    { onSearch(BookFilter.Favorites) }) {
                    BooksRoot(
                        bookListViewModel = koinViewModel(qualifier = named("favorites")),
                    )
                }
            }

            entry<FinishedRoute> {
                NavScaffold(selectedIndex, onLibrary, onFavorites, onFinished,
                    { onSearch(BookFilter.Finished) }) {
                    BooksRoot(
                        bookListViewModel = koinViewModel(qualifier = named("finished")),
                    )
                }
            }
            entry<SearchRoute> { key ->
                SearchRoot(
                    filter = key.filter,
                    onBack = { backStack.removeLastOrNull() },
                    //bookListViewModel = koinViewModel(qualifier = named("search"), parameters = { parametersOf(key.filter)})
                )
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
    onSearch: () -> Unit,
    content: @Composable () -> Unit,
) {
    AppNavigation(
        selectedIndex = selectedIndex,
        onLibrary = onLibrary,
        onFavorites = onFavorites,
        onFinished = onFinished,
        onSearch = onSearch,
        content = content,
    )
}
