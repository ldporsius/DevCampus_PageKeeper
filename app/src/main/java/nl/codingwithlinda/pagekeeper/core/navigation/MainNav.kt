package nl.codingwithlinda.pagekeeper.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import nl.codingwithlinda.pagekeeper.core.presentation.util.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.components.AppNavigation
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.BookDetailRoot
import nl.codingwithlinda.pagekeeper.feature_book_detail.chapters.ChaptersScreen
import nl.codingwithlinda.pagekeeper.feature_book_detail.chapters.ChaptersViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors.BooksRoot
import nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation.MultiSelectRoot
import nl.codingwithlinda.pagekeeper.feature_books.search.width_compact.SearchRoot
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            is ChaptersRoute -> backStack.add(ChaptersRoute(destination.ISBN))
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
                        BooksRoot(
                            onImportBook = onImportBook,
                            onNavigateToDetail = { navigate(BookDetailRoute(it)) },
                            bookListViewModel = koinViewModel(qualifier = named("all")),
                            libraryViewModel = koinViewModel(),
                        )
                    }
                )
            }
            entry<FavoritesRoute> {
                AppNavigation(
                    selectedIndex = 1,
                    content = {
                        BooksRoot(
                            onImportBook = onImportBook,
                            onNavigateToDetail = { navigate(BookDetailRoute(it)) },
                            bookListViewModel = koinViewModel(qualifier = named("favorites")),
                            libraryViewModel = koinViewModel(),
                        )
                    }
                )
            }
            entry<FinishedRoute> {
                AppNavigation(
                    selectedIndex = 2,
                    content = {
                        BooksRoot(
                            onImportBook = onImportBook,
                            onNavigateToDetail = { navigate(BookDetailRoute(it)) },
                            bookListViewModel = koinViewModel(qualifier = named("finished")),
                            libraryViewModel = koinViewModel(),
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
                    onNavigateBack = { backStack.removeLastOrNull() },
                    navToChapters = { backStack.add(ChaptersRoute(key.ISBN)) },
                    logger = koinInject(),
                )
            }
            entry<ChaptersRoute> { key ->
                val viewModel: ChaptersViewModel = koinViewModel(key = key.ISBN) { org.koin.core.parameter.parametersOf(key.ISBN) }
                ObserveAsEvents(viewModel.navChannel) {
                    backStack.removeLastOrNull()
                    backStack.add(BookDetailRoute(key.ISBN))
                }
                ChaptersScreen(
                    uiState = viewModel.uiState.collectAsStateWithLifecycle().value,
                    loadChapter = { first, last ->
                        viewModel.loadChapters(first, last) },
                    onItemClick = { sectionIndex, elementId ->
                        viewModel.updateCurrentSection(sectionIndex, elementId)
                    },
                    onToggleExpand = { viewModel.toggleExpand(it) },
                    onNavigateBack = { backStack.removeLastOrNull() },
                    scaleFactor = 1f,
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
