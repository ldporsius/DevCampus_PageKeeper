package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.model.Book

@Composable
fun LibraryRoot(
    bookRepository: BookRepository,
) {
    val viewModel = viewModel<LibraryViewModel>(
        factory = viewModelFactory {
            LibraryViewModel(bookRepository)
        }
    )

    LibraryScreen(
        books = viewModel.books.collectAsStateWithLifecycle().value
    )
}

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    books: List<Book>
) {

}