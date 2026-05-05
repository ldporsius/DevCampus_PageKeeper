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
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.util.BookImportError
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.toBookUi
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction

class LibraryViewModel(
    private val bookRepository: BookRepository,
    private val bookParser: BookParser,
    private val bookFormatValidator: BookFormatValidator,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            bookRepository.books.collect { books ->
                val lastOpened = books.filter { !it.isFinished }
                    .maxByOrNull { it.lastOpenedDate }
                    ?.takeIf { it.lastOpenedDate > 0 }
                _state.update { it.copy(lastOpenedBook = lastOpened?.toBookUi()) }
            }
        }
    }

    private val _events = Channel<LibraryEvent>()
    val events = _events.receiveAsFlow()

    private var importJob: Job? = null
    private var pendingImportBook: Book? = null
    private var duplicateBook: Book? = null

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.OnImportBookClick ->
                importJob = viewModelScope.launch {
                    if (!bookFormatValidator.isSupportedFormat(action.uri)) {
                        _state.update { it.copy(showUnsupportedFormatDialog = true) }
                        return@launch
                    }
                    _state.update { it.copy(isImporting = true) }
                    try {
                        val result = when (val parseResult = bookParser.fetch(action.uri)) {
                            is Result.Success -> {
                                val book = parseResult.data
                                val duplicate = bookRepository.findDuplicate(book.ISBN, book.title, book.author)
                                if (duplicate != null) {
                                    pendingImportBook = book
                                    duplicateBook = duplicate
                                    Result.Failure(BookImportError.BookIsDuplicate)
                                } else {
                                    parseResult
                                }
                            }
                            is Result.Failure -> parseResult
                        }
                        when (result) {
                            is Result.Success -> bookRepository.upsertBook(result.data)
                            is Result.Failure -> when (result.error) {
                                BookImportError.BookIsDuplicate ->
                                    _events.send(LibraryEvent.ShowDuplicateDialog(existing = duplicateBook!!, incoming = pendingImportBook!!))
                                BookImportError.BookImportOtherError ->
                                    _state.update { it.copy(importFailed = true) }
                            }
                        }
                    } finally {
                        _state.update { it.copy(isImporting = false) }
                    }
                }
            is LibraryAction.CancelImport ->
                importJob?.cancel()
            is LibraryAction.DismissUnsupportedFormatDialog ->
                _state.update { it.copy(showUnsupportedFormatDialog = false) }
            is LibraryAction.DismissImportFailed ->
                _state.update { it.copy(importFailed = false) }
            is LibraryAction.DismissDuplicateDialog -> {
                val pending = pendingImportBook
                if (pending != null && pending.ISBN != duplicateBook?.ISBN) {
                    viewModelScope.launch { bookRepository.deleteBook(pending.ISBN) }
                }
                pendingImportBook = null
                duplicateBook = null
            }
            is LibraryAction.ConfirmOverwriteDuplicate ->
                viewModelScope.launch {
                    val pending = pendingImportBook ?: return@launch
                    val existing = duplicateBook
                    if (existing != null && existing.ISBN != pending.ISBN) {
                        bookRepository.deleteBook(existing.ISBN)
                    }
                    val merged = pending.copy(
                        isFavorite = existing?.isFavorite ?: false,
                        isFinished = existing?.isFinished ?: false
                    )
                    bookRepository.upsertBook(merged)
                    pendingImportBook = null
                    duplicateBook = null
                }
        }
    }
}