package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import org.koin.androidx.compose.koinViewModel

@Composable
fun LibraryRoot() {
    val viewModel: LibraryViewModel = koinViewModel()

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