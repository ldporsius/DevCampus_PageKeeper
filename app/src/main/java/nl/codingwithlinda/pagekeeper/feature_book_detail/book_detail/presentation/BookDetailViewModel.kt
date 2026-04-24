package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookParseError
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.LazyBookPager
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailAction
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailState
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.ReadingMode
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.toPage
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.util.toUi
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.toBookUi

class BookDetailViewModel(
    private val isbn: String,
    private val bookRepository: BookRepository,
    private val bookPager: LazyBookPager
) : ViewModel() {

    private val _state = MutableStateFlow(BookDetailState())
    val state = _state.asStateFlow()

    private var currentSectionIndex = 0

    private suspend fun book() = bookRepository.getBookByISBN(isbn)

    init {
        viewModelScope.launch {
            val book = book() ?: return@launch

            if (!bookPager.hasPages(book)) {
                writePages(book)
            }
            bookPager.loadChapter(book, 0).collect { chapter ->
                _state.update { it.copy(pages = it.pages.plus(chapter.toPage()), isLoading = false) }
            }
        }

        bookRepository.books.onEach { list ->
            val book = list.find { it.ISBN == isbn }
            if (book != null) {
                _state.update { it.copy(book = book.toBookUi()) }
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: BookDetailAction) {
        when (action) {

            is BookDetailAction.LoadNextSection ->
                viewModelScope.launch {
                    val book = book() ?: return@launch
                    currentSectionIndex++
                    _state.update { it.copy(isLoading = true) }
                    bookPager.loadChapter(book, currentSectionIndex).collect { chapter ->
                        _state.update { it.copy(pages = it.pages.plus(chapter.toPage())) }
                    }
                    delay(500)//artificial
                    _state.update { it.copy(isLoading = false) }
                }

            BookDetailAction.ToggleReadingMode ->{
                val currentMode = _state.value.readingMode
                val newMode = if (currentMode == ReadingMode.IMMERSIVE) ReadingMode.CONTROLS else ReadingMode.IMMERSIVE
                _state.update {
                    it.copy(
                        readingMode = newMode
                    )
                }
            }
        }
    }

    private suspend fun writePages(book: Book){
        _state.update {
            it.copy(
                isLoading = false,
                isWriting = true
            )
        }
        val writeRes = bookPager.writePages(book.ISBN, book)
        when(writeRes){
            is Result.Failure -> {
                updateUiState(writeRes.error)
            }
            is Result.Success -> {
                updateUiState(null)
                bookPager.loadPages(book)
            }
        }
    }

    private fun updateUiState(error: BookParseError? = null){
        _state.update {
            it.copy(
                error = error?.toUi(),
                isLoading = false,
                isWriting = false
            )
        }
    }
}

