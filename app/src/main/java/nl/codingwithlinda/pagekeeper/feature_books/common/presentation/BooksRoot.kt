package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.FavoritesScreen
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.FinishedScreen
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryScreen
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.UnsupportedFormatDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
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
    val context = LocalContext.current
    val state by bookListViewModel.state.collectAsStateWithLifecycle()
    val libraryState by libraryViewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(libraryViewModel.events) { event ->
        when (event) {
            is LibraryEvent.NavigateToDetail -> onNavigateToDetail(event.isbn)
        }
    }

    ObserveAsEvents(bookListViewModel.shareEvents) { book ->
        val file = File(context.filesDir, "${book.isbn}.fb2")
        val fileUri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TEXT, "I'm reading \"${book.title}\" by ${book.author}")
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    if (libraryState.showUnsupportedFormatDialog) {
        UnsupportedFormatDialog(
            onDismiss = { libraryViewModel.onAction(LibraryAction.DismissUnsupportedFormatDialog) }
        )
    }

    state.bookPendingDelete?.let { book ->
        DeleteBookDialog(
            bookTitle = book.title,
            onConfirm = { bookListViewModel.onAction(BookListItemAction.ConfirmDeleteClick) },
            onDismiss = { bookListViewModel.onAction(BookListItemAction.DismissDeleteClick) }
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
