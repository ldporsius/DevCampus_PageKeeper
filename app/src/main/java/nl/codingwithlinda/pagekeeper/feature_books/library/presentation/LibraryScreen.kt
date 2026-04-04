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
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListState
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.EmptyLibraryContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction

@Composable
fun LibraryScreen(
    bookListState: BookListState,
    onImportBook: () -> Unit,
    onAction: (LibraryAction) -> Unit,
    onItemAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
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
