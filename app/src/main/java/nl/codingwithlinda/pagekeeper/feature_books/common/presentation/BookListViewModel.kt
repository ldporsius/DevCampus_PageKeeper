package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction

class BookListViewModel(
    private val bookRepository: BookRepository,
    private val filter: BookFilter
) : ViewModel() {

    private val _state = MutableStateFlow(BookListState())
    val state = _state.asStateFlow()

    init {
        bookRepository.books
            .onEach { list ->
                _state.update { current ->
                    current.copy(
                        books = list
                            .filter { book ->
                                when (filter) {
                                    BookFilter.All -> true
                                    BookFilter.Favorites -> book.isFavorite
                                    BookFilter.Finished -> book.isFinished
                                }
                            }
                            .map { it.toBookUi() }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: BookListItemAction) {
        when (action) {
            is BookListItemAction.DeleteClick -> {
                val book = _state.value.books.find { it.isbn == action.isbn }
                _state.update { it.copy(bookPendingDelete = book) }
            }
            is BookListItemAction.ConfirmDeleteClick -> {
                val isbn = _state.value.bookPendingDelete?.isbn ?: return
                _state.update { it.copy(bookPendingDelete = null) }
                viewModelScope.launch { bookRepository.deleteBook(isbn) }
            }
            is BookListItemAction.DismissDeleteClick ->
                _state.update { it.copy(bookPendingDelete = null) }
            is BookListItemAction.FavouriteClick -> viewModelScope.launch {
                bookRepository.getBookByISBN(action.isbn)?.let { book ->
                    bookRepository.upsertBook(book.copy(isFavorite = !book.isFavorite))
                }
            }
            is BookListItemAction.ReadingClick -> Unit // TODO
            is BookListItemAction.ShareClick -> Unit   // TODO
        }
    }
}