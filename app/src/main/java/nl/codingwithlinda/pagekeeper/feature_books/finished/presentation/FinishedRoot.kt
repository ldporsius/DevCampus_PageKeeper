package nl.codingwithlinda.pagekeeper.feature_books.finished.presentation

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
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListItem
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListState
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.DeleteBookDialog
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.components.EmptyFinishedContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun FinishedRoot(
    viewModel: BookListViewModel = koinViewModel(qualifier = named(BookFilter.Finished))
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FinishedScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun FinishedScreen(
    state: BookListState,
    onAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
    state.bookPendingDelete?.let { book ->
        DeleteBookDialog(
            bookTitle = book.title,
            onConfirm = { onAction(BookListItemAction.ConfirmDeleteClick) },
            onDismiss = { onAction(BookListItemAction.DismissDeleteClick) }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.books.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyFinishedContent()
        }

        AnimatedVisibility(
            visible = state.books.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = state.books, key = { it.isbn }) { book ->
                    BookListItem(
                        book = book,
                        onAction = onAction
                    )
                }
            }
        }
    }
}