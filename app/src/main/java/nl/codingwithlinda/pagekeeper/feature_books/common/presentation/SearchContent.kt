package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun SearchContent(books: List<BookUi>) {
    val viewModel: BookListViewModel = koinViewModel(qualifier = named(BookFilter.All))

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(books, key = { it.isbn }) { book ->
            BookListItem(
                book = book,
                onAction = viewModel::onAction
            )
        }
    }
}
