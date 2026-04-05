package nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.toBookUi
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction

class MultiSelectViewModel(
    val savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository
) : ViewModel() {

    companion object {
        const val KEY_FILTER = "MULTI_SELECT_KEY_FILTER"
    }

    private val _state = MutableStateFlow(MultiSelectState())
    val state = _state.asStateFlow()

    private val _events = Channel<MultiSelectEvent>()
    val events = _events.receiveAsFlow()

    init {
        val filterFlow = savedStateHandle.getStateFlow(KEY_FILTER, BookFilter.All)

        bookRepository.books
            .combine(filterFlow) { books, filter ->
                _state.update { current ->
                    current.copy(
                        books = books
                            .filter { book ->
                                when (filter) {
                                    BookFilter.All -> true
                                    BookFilter.Favorites -> book.isFavorite
                                    BookFilter.Finished -> book.isFinished
                                }
                            }
                            .sortedByDescending { it.dateCreated }
                            .map { it.toBookUi() }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: MultiSelectAction) {
        when (action) {
            is MultiSelectAction.ToggleBook -> {
                _state.update { current ->
                    val updated = if (action.isbn in current.selectedIsbn) {
                        current.selectedIsbn - action.isbn
                    } else {
                        current.selectedIsbn + action.isbn
                    }
                    current.copy(selectedIsbn = updated)
                }
            }

            MultiSelectAction.AddToFavorites -> {
                val selected = _state.value.selectedBooks
                viewModelScope.launch {
                    selected.forEach { bookUi ->
                        bookRepository.getBookByISBN(bookUi.isbn)?.let { book ->
                            bookRepository.upsertBook(book.copy(isFavorite = true))
                        }
                    }
                }
            }

            MultiSelectAction.Share -> {
                val selected = _state.value.selectedBooks
                if (selected.isEmpty()) return
                viewModelScope.launch { _events.send(MultiSelectEvent.ShareBooks(selected)) }
            }

            MultiSelectAction.Delete -> {
                if (_state.value.selectedIsbn.isEmpty()) return
                _state.update { it.copy(showDeleteConfirmation = true) }
            }

            MultiSelectAction.ConfirmDelete -> {
                val toDelete = _state.value.selectedIsbn.toList()
                _state.update { it.copy(showDeleteConfirmation = false, selectedIsbn = emptySet()) }
                viewModelScope.launch {
                    toDelete.forEach { bookRepository.deleteBook(it) }
                }
            }

            MultiSelectAction.DismissDelete ->
                _state.update { it.copy(showDeleteConfirmation = false) }

            MultiSelectAction.NavigateBack ->
                viewModelScope.launch { _events.send(MultiSelectEvent.NavigateBack) }
        }
    }

    fun onBookAction(action: BookListItemAction) {
        when (action) {
            is BookListItemAction.FavouriteClick -> viewModelScope.launch {
                bookRepository.getBookByISBN(action.isbn)?.let { book ->
                    bookRepository.upsertBook(book.copy(isFavorite = !book.isFavorite))
                }
            }
            is BookListItemAction.FinishClick -> viewModelScope.launch {
                bookRepository.getBookByISBN(action.isbn)?.let { book ->
                    bookRepository.upsertBook(book.copy(isFinished = !book.isFinished))
                }
            }
            is BookListItemAction.ShareClick -> {
                val book = _state.value.books.find { it.isbn == action.isbn } ?: return
                viewModelScope.launch { _events.send(MultiSelectEvent.ShareBooks(listOf(book))) }
            }
            is BookListItemAction.DeleteClick -> {
                val book = _state.value.books.find { it.isbn == action.isbn } ?: return
                _state.update { it.copy(bookPendingDelete = book) }
            }
            is BookListItemAction.ConfirmDeleteClick -> {
                val isbn = _state.value.bookPendingDelete?.isbn ?: return
                _state.update { it.copy(bookPendingDelete = null) }
                viewModelScope.launch { bookRepository.deleteBook(isbn) }
            }
            is BookListItemAction.DismissDeleteClick ->
                _state.update { it.copy(bookPendingDelete = null) }
            is BookListItemAction.MultiSelectLongPress -> Unit
        }
    }
}
