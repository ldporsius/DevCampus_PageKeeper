package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

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
import nl.codingwithlinda.pagekeeper.core.domain.util.Logger
import nl.codingwithlinda.pagekeeper.core.navigation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.navigation.NavigationMenuAction
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import nl.codingwithlinda.pagekeeper.core.navigation.MultiSelectRoute
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookPager

class BookListViewModel(
    val savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val bookPager: BookPager,
    private val menuActionController: MenuActionController,
    private val logger: Logger,
    initialFilter: BookFilter = BookFilter.All
) : ViewModel() {

    companion object{
        const val KEY_FILTER = "KEY_FILTER"
    }
    private val _state = MutableStateFlow(BookListState())
    val state = _state.asStateFlow()

    private val _shareEvents = Channel<BookUi>()
    val shareEvents = _shareEvents.receiveAsFlow()


   init {
        // Set the filter synchronously before getStateFlow is called so the first
        // combine emission uses the correct filter — no double-emission flash.
        // Skip if already present (process restoration via SavedStateHandle).
        if (!savedStateHandle.contains(KEY_FILTER)) {
            logger.log("--- BOOKLISTVIEWMODEL--- Setting filter to $initialFilter")
            savedStateHandle[KEY_FILTER] = initialFilter
        }

        val savedFilter = savedStateHandle.getStateFlow(KEY_FILTER, initialFilter)

        bookRepository.books.combine(savedFilter) { list , sFilter->
                _state.update { current ->
                    current.copy(
                        filter = sFilter,
                        isLoading = false,
                        books = list
                            .filter { book ->
                                when (sFilter) {
                                    BookFilter.All -> true
                                    BookFilter.Favorites -> book.isFavorite
                                    BookFilter.Finished -> book.isFinished
                                }
                            }
                            .sortedByDescending { it.dateCreated }
                            .map { book ->
                                val total = if (bookPager.hasElementMeta(book.ISBN)) bookPager.countElements(book.ISBN) else bookPager.countPages(book.ISBN)
                                val current = if (bookPager.hasElementMeta(book.ISBN)) book.currentElementId.toFloat() else book.currentSection.toFloat()
                                val actualReadingProgress = current / total
                                val readingProgress = if (book.isFinished) 1f else actualReadingProgress
                                book.toBookUi()
                                    .copy(
                                        readingProgress = readingProgress
                                    )
                            }

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
            is BookListItemAction.FinishClick -> viewModelScope.launch{
                bookRepository.getBookByISBN(action.isbn)?.let { book ->
                    bookRepository.upsertBook(
                        book.copy(
                            isFinished = !book.isFinished
                        )
                    )
                }
            }
            is BookListItemAction.ShareClick -> {
                val book = _state.value.books.find { it.isbn == action.isbn } ?: return
                viewModelScope.launch { _shareEvents.send(book) }
            }

            is BookListItemAction.MultiSelectLongPress -> {
                val filter = savedStateHandle.get<BookFilter>(KEY_FILTER) ?: BookFilter.All
                viewModelScope.launch {
                    menuActionController.onAction(NavigationMenuAction(MultiSelectRoute(filter)))
                }
            }

        }
    }
}