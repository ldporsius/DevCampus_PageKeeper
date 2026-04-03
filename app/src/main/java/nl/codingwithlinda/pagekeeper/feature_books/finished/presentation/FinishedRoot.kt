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
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.components.EmptyFinishedContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun FinishedRoot(
    viewModel: BookListViewModel = koinViewModel(qualifier = named(BookFilter.Finished))
) {
    val books by viewModel.books.collectAsStateWithLifecycle()

    FinishedScreen(
        books = books,
        onAction = viewModel::onAction
    )
}

@Composable
fun FinishedScreen(
    books: List<BookUi>,
    onAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = books.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyFinishedContent()
        }

        AnimatedVisibility(
            visible = books.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = books, key = { it.isbn }) { book ->
                    BookListItem(
                        book = book,
                        onAction = onAction
                    )
                }
            }
        }
    }
}