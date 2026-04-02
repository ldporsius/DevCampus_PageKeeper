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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import org.koin.androidx.compose.koinViewModel

@Composable
fun LibraryRoot() {
    val viewModel: LibraryViewModel = koinViewModel()

    LibraryScreen(
        books = viewModel.books.collectAsStateWithLifecycle().value,
        onImportBook = {}
    )
}

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    books: List<Book>,
    onImportBook: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = books.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyLibraryContent(onImportBook = onImportBook)
        }

        AnimatedVisibility(
            visible = books.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = books, key = { it.ISBN }) { book ->
                    BookListItem(book = book)
                }
            }
        }
    }
}