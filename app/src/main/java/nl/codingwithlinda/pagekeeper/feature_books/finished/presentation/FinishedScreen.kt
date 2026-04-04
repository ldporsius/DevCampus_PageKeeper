package nl.codingwithlinda.pagekeeper.feature_books.finished.presentation

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
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.components.EmptyFinishedContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction

@Composable
fun FinishedScreen(
    state: BookListState,
    onAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
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
