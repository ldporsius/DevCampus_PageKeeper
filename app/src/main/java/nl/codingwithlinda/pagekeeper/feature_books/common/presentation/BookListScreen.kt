package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction

@Composable
fun BookListScreen(
    state: BookListState,
    onAction: (BookListItemAction) -> Unit,
    onBookClick: (String) -> Unit,
    emptyContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
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
                onBookClick = onBookClick,
                onAction = onAction
            )
        }
    }
}