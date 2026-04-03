package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction

class LibraryViewModel(
    private val bookRepository: BookRepository,
    private val bookParser: BookParser
) : ViewModel() {

    private val _events = Channel<LibraryEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.OnBookClick ->
                viewModelScope.launch { _events.send(LibraryEvent.NavigateToDetail(action.isbn)) }
            is LibraryAction.OnImportBookClick ->
                viewModelScope.launch {
                    bookParser.fetch("action.uri")?.let {
                        bookRepository.upsertBook(it)
                    }
                }
        }
    }
}