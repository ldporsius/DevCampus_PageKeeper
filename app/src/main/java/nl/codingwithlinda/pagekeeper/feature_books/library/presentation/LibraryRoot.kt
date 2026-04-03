package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListItem
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListState
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.DeleteBookDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.EmptyLibraryContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.UnsupportedFormatDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction

import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun LibraryRoot(
    onNavigateToDetail: (String) -> Unit,
    onImportBook: () -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named(BookFilter.All))
) {
    val bookListState by bookListViewModel.state.collectAsStateWithLifecycle()
    val libraryState by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LibraryEvent.NavigateToDetail -> onNavigateToDetail(event.isbn)
        }
    }

    if (libraryState.showUnsupportedFormatDialog) {
        UnsupportedFormatDialog(
            onDismiss = { viewModel.onAction(LibraryAction.DismissUnsupportedFormatDialog) }
        )
    }

    LibraryScreen(
        bookListState = bookListState,
        onImportBook = onImportBook,
        onAction = viewModel::onAction,
        onItemAction = bookListViewModel::onAction
    )
}

@Composable
fun LibraryScreen(
    bookListState: BookListState,
    onImportBook: () -> Unit,
    onAction: (LibraryAction) -> Unit,
    onItemAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
    bookListState.bookPendingDelete?.let { book ->
        DeleteBookDialog(
            bookTitle = book.title,
            onConfirm = { onItemAction(BookListItemAction.ConfirmDeleteClick) },
            onDismiss = { onItemAction(BookListItemAction.DismissDeleteClick) }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = bookListState.books.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyLibraryContent(onImportBook = onImportBook)
        }

        AnimatedVisibility(
            visible = bookListState.books.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = bookListState.books, key = { it.isbn }) { book ->
                    BookListItem(
                        book = book,
                        onClick = { onAction(LibraryAction.OnBookClick(book.isbn)) },
                        onAction = onItemAction
                    )
                }
            }
        }
    }
}