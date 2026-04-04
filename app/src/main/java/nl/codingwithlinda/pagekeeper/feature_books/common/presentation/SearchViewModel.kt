package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository

@OptIn(FlowPreview::class)
class SearchViewModel(
    bookRepository: BookRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val state = combine(
        bookRepository.books,
        _query,
        _query.debounce { if (it.isBlank()) 0L else 500L }
    ) { books, query, debouncedQuery ->
        SearchState(
            query = query,
            books = books
                .sortedByDescending { it.dateCreated }
                .map { it.toBookUi() }
                .filter { book ->
                    debouncedQuery.isBlank() ||
                            book.title.contains(debouncedQuery, ignoreCase = true) ||
                            book.author.contains(debouncedQuery, ignoreCase = true)
                }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchState()
    )

    fun onQueryChange(query: String) {
        _query.value = query
    }
}
