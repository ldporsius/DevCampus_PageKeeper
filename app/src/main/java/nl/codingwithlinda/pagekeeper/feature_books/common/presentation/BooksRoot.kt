package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.FavoritesScreen
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.FinishedScreen
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryScreen
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.UnsupportedFormatDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun BooksRoot(
    activeFilter: BookFilter,
    onNavigateToDetail: (String) -> Unit = {},
    onImportBook: () -> Unit = {},
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named(activeFilter)),
    libraryViewModel: LibraryViewModel = koinViewModel()
) {
    val state by bookListViewModel.state.collectAsStateWithLifecycle()
    val libraryState by libraryViewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(libraryViewModel.events) { event ->
        when (event) {
            is LibraryEvent.NavigateToDetail -> onNavigateToDetail(event.isbn)
        }
    }

    BookListSideEffects(bookListViewModel)

    if (libraryState.showUnsupportedFormatDialog) {
        UnsupportedFormatDialog(
            onDismiss = { libraryViewModel.onAction(LibraryAction.DismissUnsupportedFormatDialog) }
        )
    }

    when (activeFilter) {
        BookFilter.All -> LibraryScreen(
            state = state,
            onImportBook = onImportBook,
            onLibraryAction = libraryViewModel::onAction,
            onAction = bookListViewModel::onAction
        )
        BookFilter.Favorites -> FavoritesScreen(
            state = state,
            onAction = bookListViewModel::onAction
        )
        BookFilter.Finished -> FinishedScreen(
            state = state,
            onAction = bookListViewModel::onAction
        )
    }
}
