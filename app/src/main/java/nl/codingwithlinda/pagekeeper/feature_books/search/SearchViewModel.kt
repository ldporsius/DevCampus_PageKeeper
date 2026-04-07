package nl.codingwithlinda.pagekeeper.feature_books.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListState
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel.Companion.KEY_FILTER
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.toBookUi

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    bookRepository: BookRepository
) : ViewModel() {

    private val filter = savedStateHandle.getStateFlow(KEY_FILTER, BookFilter.All)

    fun setFilter(filter: BookFilter) {
        savedStateHandle[KEY_FILTER] = filter
    }


    private val filteredBooks = bookRepository.books.combine(filter){ books, filter ->
        when(filter){
            BookFilter.All -> books
            BookFilter.Favorites -> books.filter { it.isFavorite }
            BookFilter.Finished -> books.filter { it.isFinished }
        }.let { bookEntities ->
            BookListState(
                isLoading = false,
                filter = filter,
                query = _query.value,
                books = bookEntities.map { it.toBookUi() })
        }
    }
    private val _query = MutableStateFlow("")

    val state = combine(
        filteredBooks,
        _query,
        _query.debounce { if (it.isBlank()) 0L else 500L }
    ) { books, query, debouncedQuery ->
        books.copy(
            isLoading = false,
            query = query,
            books = books.books
                .filter { book ->
                    debouncedQuery.isBlank() ||
                            book.title.contains(debouncedQuery, ignoreCase = true) ||
                            book.author.contains(debouncedQuery, ignoreCase = true)
                }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000),
        initialValue = BookListState()
    )

    fun onQueryChange(query: String) {
        _query.value = query
    }
}