package nl.codingwithlinda.pagekeeper.core.navigation

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowWidthSizeClass
import nl.codingwithlinda.pagekeeper.core.presentation.util.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.components.AppNavigation
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.BookDetailRoot
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors.BooksRoot
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors.BooksRootExpandedWidth
import nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation.MultiSelectRoot
import nl.codingwithlinda.pagekeeper.feature_books.search.SearchViewModel
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
        when (destination) {
            is BookListRoute -> {
                backStack.add(BookListRoute); backStack.retainAll { it is BookListRoute }
            }
            is FavoritesRoute -> {
                backStack.add(FavoritesRoute); backStack.retainAll { it is FavoritesRoute }
            }
            is FinishedRoute -> {
                backStack.add(FinishedRoute); backStack.retainAll { it is FinishedRoute }
            }
            is SearchRoute -> backStack.add(SearchRoute(destination.filter))
            is MultiSelectRoute -> backStack.add(MultiSelectRoute(destination.filter))
            is BookDetailRoute -> backStack.add(BookDetailRoute(destination.ISBN))
        }
    }

    ObserveAsEvents(controller.actions) { action ->
        when (action) {
            ImportBookMenuAction -> {
                navigate(BookListRoute)
                onImportBook()
            }
            is NavigationMenuAction -> navigate(action.destination)
        }
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<BookListRoute> {
                AppNavigation(
                    selectedIndex = 0,
                    content = {
                        val viewModel = koinViewModel<BookListViewModel>(qualifier = named("all"))
                        val isExpandedWidth = currentWindowAdaptiveInfo()
                            .windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
                        if (isExpandedWidth) {
                            val searchViewModel = koinViewModel<SearchViewModel>(key = "search_all")
                            searchViewModel.setFilter(BookFilter.All)
                            BooksRootExpandedWidth(
                                onImportBook = onImportBook,
                                onNavigateToDetail = { navigate(BookDetailRoute(it)) },
                                bookListViewModel = viewModel,
                                searchViewModel = searchViewModel
                            )
                        } else {
                            BooksRoot(
                                onImportBook = onImportBook,
                                onNavigateToDetail = { navigate(BookDetailRoute(it)) },
                                bookListViewModel = viewModel,
                                libraryViewModel = koinViewModel(),
                            )
                        }
                    }
                )
            }
            entry<FavoritesRoute> {
                val viewModel = koinViewModel<BookListViewModel>(qualifier = named("favorites"))
                AppNavigation(
                    selectedIndex = 1,
                    content = {
                        val isExpandedWidth = currentWindowAdaptiveInfo()
                            .windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
                        if (isExpandedWidth) {
                            val searchViewModel = koinViewModel<SearchViewModel>(key = "search_favorites")
                            searchViewModel.setFilter(BookFilter.Favorites)
                            BooksRootExpandedWidth(
                                onImportBook = onImportBook,
                                onNavigateToDetail = { navigate(BookDetailRoute(it)) },
                                bookListViewModel = viewModel,
                                searchViewModel = searchViewModel
                            )
                        } else {
                            BooksRoot(
                                onImportBook = onImportBook,
                                onNavigateToDetail = { navigate(BookDetailRoute(it)) },
                                bookListViewModel = viewModel,
                                libraryViewModel = koinViewModel(),
                            )
                        }
                    }
                )

            }
            entry<FinishedRoute> {
                val viewModel = koinViewModel<BookListViewModel>(qualifier = named("finished"))
                AppNavigation(
                    selectedIndex = 2,
                    content = {
                        val isExpandedWidth = currentWindowAdaptiveInfo()
                            .windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
                        if (isExpandedWidth) {
                            val searchViewModel = koinViewModel<SearchViewModel>(key = "search_finished")
                            searchViewModel.setFilter(BookFilter.Finished)
                            BooksRootExpandedWidth(
                                onImportBook = onImportBook,
                                onNavigateToDetail = { navigate(BookDetailRoute(it)) },
                                bookListViewModel = viewModel,
                                searchViewModel = searchViewModel
                            )
                        } else {
                            BooksRoot(
                                onImportBook = onImportBook,
                                onNavigateToDetail = { navigate(BookDetailRoute(it)) },
                                bookListViewModel = viewModel,
                                libraryViewModel = koinViewModel(),
                            )
                        }
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
