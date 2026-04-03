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
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.components.EmptyFinishedContent
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.interaction.FinishedState
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.BookListItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun FinishedRoot(
    viewModel: FinishedViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FinishedScreen(state = state)
}

@Composable
fun FinishedScreen(
    state: FinishedState,
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
                    BookListItem(book = book)
                }
            }
        }
    }
}