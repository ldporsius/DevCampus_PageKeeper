package nl.codingwithlinda.pagekeeper.feature_books.search.width_compact

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListItem
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi
import nl.codingwithlinda.pagekeeper.feature_books.search.components.EmptySearchComponent
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun SearchContent(
    query: String,
    books: List<BookUi>
) {
    val viewModel: BookListViewModel = koinViewModel(qualifier = named(BookFilter.All))

    if (query.isNotBlank() && books.isEmpty()) {
        EmptySearchComponent()
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(books, key = { it.isbn }) { book ->
                BookListItem(
                    book = book,
                    onAction = viewModel::onAction
                )
            }
        }
    }
}
