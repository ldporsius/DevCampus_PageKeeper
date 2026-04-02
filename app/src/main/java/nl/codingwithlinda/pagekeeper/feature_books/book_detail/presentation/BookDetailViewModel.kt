package nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.navigation.BookDetailEvent
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.toBookUi

class BookDetailViewModel(
    private val isbn: String,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BookDetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<BookDetailEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val book = bookRepository.getBookByISBN(isbn)
            _state.update { it.copy(book = book?.toBookUi(), isLoading = false) }
        }
    }

    fun onAction(action: BookDetailAction) {
        when (action) {
            is BookDetailAction.OnBackClick ->
                viewModelScope.launch { _events.send(BookDetailEvent.NavigateBack) }
        }
    }
}