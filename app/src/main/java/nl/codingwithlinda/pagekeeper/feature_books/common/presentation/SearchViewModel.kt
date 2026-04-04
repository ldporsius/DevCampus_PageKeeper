package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository

class SearchViewModel(
    bookRepository: BookRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val state = combine(bookRepository.books, _query) { books, query ->
        SearchState(
            query = query,
            books = books
                .sortedByDescending { it.dateCreated }
                .map { it.toBookUi() }
                .filter { book ->
                    query.isBlank() ||
                            book.title.contains(query, ignoreCase = true) ||
                            book.author.contains(query, ignoreCase = true)
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
