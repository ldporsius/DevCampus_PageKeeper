package nl.codingwithlinda.pagekeeper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors.BooksRoot
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors.BooksRootExpandedWidth
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.FavoritesScreen
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.FinishedScreen
import nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation.MultiSelectRoot
import nl.codingwithlinda.pagekeeper.feature_books.search.width_compact.SearchRoot
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun MainNav(
    onImportBook: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val backStack = rememberNavBackStack(BookListRoute)
    val controller = koinInject<MenuActionController>()


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

    fun onSearch(filter: BookFilter) { navigate(SearchRoute(filter)) }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<BookListRoute> {
                val isExpandedWidth = currentWindowAdaptiveInfo()
                    .windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
                AppNavigation(
                    selectedIndex = 0,
                    content = {
                        if (isExpandedWidth) {
                            BooksRootExpandedWidth(onImportBook = onImportBook)
                        } else {
                            BooksRoot(onImportBook = onImportBook)
                        }
                    }
                )
            }

            entry<FavoritesRoute> {
                val viewModel = koinViewModel<BookListViewModel>(qualifier = named("favorites"))
                viewModel.setFilter(BookFilter.Favorites)
                AppNavigation(
                    selectedIndex = 1,
                    content = {
                        FavoritesScreen(
                            state = viewModel.state.collectAsStateWithLifecycle().value,
                            onAction = viewModel::onAction
                        )
                    }
                )
            }

            entry<FinishedRoute> {
                val viewModel = koinViewModel<BookListViewModel>(qualifier = named("finished"))
                viewModel.setFilter(BookFilter.Finished)
                AppNavigation(
                    selectedIndex = 2,
                    content = {
                        FinishedScreen(
                            state = viewModel.state.collectAsStateWithLifecycle().value,
                            onAction = viewModel::onAction
                        )
                    }
                )

            }
            entry<SearchRoute> { key ->
                SearchRoot(
                    filter = key.filter,
                    onBack = { backStack.removeLastOrNull() },
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

