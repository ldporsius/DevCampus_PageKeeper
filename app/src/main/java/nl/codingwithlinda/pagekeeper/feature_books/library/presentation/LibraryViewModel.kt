package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookFormatValidator
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction

class LibraryViewModel(
    private val bookRepository: BookRepository,
    private val bookParser: BookParser,
    private val bookFormatValidator: BookFormatValidator
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state = _state.asStateFlow()

    private val _events = Channel<LibraryEvent>()
    val events = _events.receiveAsFlow()

    private var importJob: Job? = null

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.OnBookClick ->
                viewModelScope.launch { _events.send(LibraryEvent.NavigateToDetail(action.isbn)) }
            is LibraryAction.OnImportBookClick ->
                importJob = viewModelScope.launch {
                    if (!bookFormatValidator.isSupportedFormat(action.uri)) {
                        _state.update { it.copy(showUnsupportedFormatDialog = true) }
                        return@launch
                    }
                    _state.update { it.copy(isImporting = true) }
                    try {
                        val book = bookParser.fetch(action.uri)
                        if (book != null) {
                            bookRepository.upsertBook(book)
                        } else {
                            _state.update { it.copy(importFailed = true) }
                        }
                    } finally {
                        _state.update { it.copy(isImporting = false) }
                    }
                }
            is LibraryAction.CancelImport -> {
                importJob?.cancel()
            }
            is LibraryAction.DismissUnsupportedFormatDialog ->
                _state.update { it.copy(showUnsupportedFormatDialog = false) }
            is LibraryAction.DismissImportFailed ->
                _state.update { it.copy(importFailed = false) }
        }
    }
}