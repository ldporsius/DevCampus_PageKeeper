package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListItem
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListItemPlaceholder
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListState
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.EmptyLibraryContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction

@Composable
fun LibraryScreen(
    state: BookListState,
    isImporting: Boolean,
    onImportBook: () -> Unit,
    onCancelImport: () -> Unit,
    onLibraryAction: (LibraryAction) -> Unit,
    onAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.books.isEmpty() && !isImporting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyLibraryContent(onImportBook = onImportBook)
        }

        AnimatedVisibility(
            visible = state.books.isNotEmpty() || isImporting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (isImporting) {
                    item(key = "importing_placeholder") {
                        BookListItemPlaceholder(onCancel = onCancelImport)
                    }
                }
                items(items = state.books, key = { it.isbn }) { book ->
                    BookListItem(
                        book = book,
                        onClick = { onLibraryAction(LibraryAction.OnBookClick(book.isbn)) },
                        onAction = onAction
                    )
                }
            }
        }
    }
}