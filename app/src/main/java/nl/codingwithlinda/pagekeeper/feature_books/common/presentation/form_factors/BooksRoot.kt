package nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListSideEffects
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.FavoritesScreen
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.FinishedScreen
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryScreen
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun BooksRoot(
    onImportBook: () -> Unit = {},
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named("all")),
    libraryViewModel: LibraryViewModel = koinViewModel()
) {

    val state by bookListViewModel.state.collectAsStateWithLifecycle()
    val libraryState by libraryViewModel.state.collectAsStateWithLifecycle()

    BookListSideEffects(bookListViewModel)

    LibraryScreen(
        state = state,
        libraryState = libraryState,
        isImporting = libraryState.isImporting,
        onImportBook = onImportBook,
        onCancelImport = { libraryViewModel.onAction(LibraryAction.CancelImport) },
        onLibraryAction = libraryViewModel::onAction,
        onAction = bookListViewModel::onAction
    )
}
