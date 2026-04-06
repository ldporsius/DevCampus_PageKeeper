package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.FavoritesScreen
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.FinishedScreen
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryScreen
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.ImportFailedDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.UnsupportedFormatDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BooksRoot(
    //activeFilter: BookFilter,
    onNavigateToDetail: (String) -> Unit = {},
    onImportBook: () -> Unit = {},
    bookListViewModel: BookListViewModel = koinViewModel { parametersOf(BookFilter.All) },
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


    when (state.filter) {
        BookFilter.All -> LibraryScreen(
            state = state,
            libraryState = libraryState,
            isImporting = libraryState.isImporting,
            onImportBook = onImportBook,
            onCancelImport = { libraryViewModel.onAction(LibraryAction.CancelImport) },
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
