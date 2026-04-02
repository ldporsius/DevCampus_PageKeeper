package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.toBookUi
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryState

class LibraryViewModel(
    private val bookRepository: BookRepository,
    private val bookParser: BookParser
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state = _state.asStateFlow()

    private val _events = Channel<LibraryEvent>()
    val events = _events.receiveAsFlow()

    init {
        bookRepository.books
            .onEach { books ->
                _state.update { it.copy(books = books.map { b -> b.toBookUi() }) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.OnBookClick ->
                viewModelScope.launch { _events.send(LibraryEvent.NavigateToDetail(action.isbn)) }
            is LibraryAction.OnDeleteClick ->
                viewModelScope.launch { bookRepository.deleteBook(action.isbn) }
            is LibraryAction.OnImportBookClick ->{
                viewModelScope.launch {
                    bookParser.fetch("action.uri")?.let {
                        bookRepository.upsertBook(it)
                    }
                }
            }
            is LibraryAction.OnFavouriteClick -> Unit  // TODO
            is LibraryAction.OnReadingClick -> Unit    // TODO
            is LibraryAction.OnShareClick -> Unit      // TODO
        }
    }
}