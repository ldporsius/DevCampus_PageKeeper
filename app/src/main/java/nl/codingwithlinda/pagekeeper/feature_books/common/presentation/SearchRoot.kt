package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.design_system.components.SearchScaffold
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun SearchRoot(
    onBack: () -> Unit,
    searchViewModel: SearchViewModel = koinViewModel(),
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named(BookFilter.All))
) {
    val state by searchViewModel.state.collectAsStateWithLifecycle()

    BookListSideEffects(bookListViewModel)

    SearchScaffold(
        query = state.query,
        onQueryChange = searchViewModel::onQueryChange,
        onBack = onBack
    ) {
        SearchContent(books = state.books)
    }
}
