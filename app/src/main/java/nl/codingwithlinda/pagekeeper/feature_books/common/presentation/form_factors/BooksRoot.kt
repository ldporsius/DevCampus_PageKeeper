package nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.navigation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.navigation.NavigationMenuAction
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookImportSideEffects
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.components.BookItemsGrid
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListSideEffects
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.components.EmptyFavoritesContent
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.components.EmptyFinishedContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.EmptyLibraryContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import nl.codingwithlinda.pagekeeper.core.navigation.BookDetailRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun BooksRoot(
    onImportBook: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named("all")),
    libraryViewModel: LibraryViewModel = koinViewModel()
) {
    val state by bookListViewModel.state.collectAsStateWithLifecycle()
    val libraryState by libraryViewModel.state.collectAsStateWithLifecycle()

    val controller = koinInject<MenuActionController>()
    val scope = rememberCoroutineScope()

    val emptyContent: @Composable ()-> Unit = {
            when(state.filter){
                BookFilter.All -> EmptyLibraryContent() { onImportBook()}
                BookFilter.Favorites -> EmptyFavoritesContent()
                BookFilter.Finished -> EmptyFinishedContent()
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        BookImportSideEffects(onNavigateToDetail = onNavigateToDetail)
        BookListSideEffects(bookListViewModel)

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = state.books.isEmpty() && !state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                emptyContent()
            }
            AnimatedVisibility(
                visible = state.books.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BookItemsGrid(
                    books = state.books,
                    isImporting = libraryState.isImporting,
                    onCancelImport = {
                        libraryViewModel.onAction(LibraryAction.CancelImport)
                    },
                    onBookClick = {
                            isbn -> scope.launch { controller.onAction(NavigationMenuAction(BookDetailRoute(isbn))) }
                    },
                    onAction = bookListViewModel::onAction
                )
            }
        }
    }

}
