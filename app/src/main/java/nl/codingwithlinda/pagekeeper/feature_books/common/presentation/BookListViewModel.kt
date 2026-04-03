package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction

class BookListViewModel(
    private val bookRepository: BookRepository,
    private val filter: BookFilter
) : ViewModel() {

    val books = bookRepository.books
        .map { list ->
            list
                .filter { book ->
                    when (filter) {
                        BookFilter.All -> true
                        BookFilter.Favorites -> book.isFavorite
                        BookFilter.Finished -> book.isFinished
                    }
                }
                .map { it.toBookUi() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onAction(action: BookListItemAction) {
        when (action) {
            is BookListItemAction.FavouriteClick -> viewModelScope.launch {
                bookRepository.getBookByISBN(action.isbn)?.let { book ->
                    bookRepository.upsertBook(book.copy(isFavorite = !book.isFavorite))
                }
            }
            is BookListItemAction.ReadingClick -> Unit // TODO
            is BookListItemAction.ShareClick -> Unit   // TODO
            is BookListItemAction.DeleteClick -> viewModelScope.launch {
                bookRepository.deleteBook(action.isbn)
            }
        }
    }
}