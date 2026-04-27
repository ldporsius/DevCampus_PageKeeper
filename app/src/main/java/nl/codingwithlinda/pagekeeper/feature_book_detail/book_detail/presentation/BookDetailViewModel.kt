package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.Page
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.toPage
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.util.toUi
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.toBookUi

class BookDetailViewModel(
    private val isbn: String,
    private val bookRepository: BookRepository,
    private val bookPager: LazyBookPager
) : ViewModel() {

    private val _state = MutableStateFlow(BookDetailState())
    val state = _state
        .onEach {
            println("---BOOK DETAIL VIEW MODEL --- PAGES loaded: ${it.pages.count { (_, p) -> p !is Page.Loading }} / ${it.pages.size}")
        }
        .onStart {
            _state.update { it.copy(isLoading = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), BookDetailState())

    private val sectionsLoading = mutableSetOf<Int>()

    private suspend fun book() = bookRepository.getBookByISBN(isbn)

    init {
        viewModelScope.launch {
            val book = book() ?: return@launch

            _state.update { it.copy(book = book.toBookUi()) }

            if (!bookPager.hasPages(book)) {
                writePages(book)
            }

            val totalSections = bookPager.countPages(book)
            val initialSection = book.currentSection
            val initialSectionOffset = book.currentSectionOffset

            val loadingPages = (0 until totalSections).associate { i -> i to Page.Loading(i) }

            _state.update {
                it.copy(
                    pages = loadingPages,
                    currentSection = initialSection,
                    currentSectionOffset = initialSectionOffset,
                    totalSections = totalSections,
                    isLoading = false,
                )
            }
        }
    }

    fun onAction(action: BookDetailAction) {
        when (action) {
            is BookDetailAction.LoadSection -> {
                if (_state.value.pages[action.sectionId] !is Page.Loading) return
                if (!sectionsLoading.add(action.sectionId)) return
                viewModelScope.launch {
                    try {
                        val book = book() ?: return@launch
                        _state.update {
                            it.copy(isLoading = true)
                        }
                        delay(2000)
                        bookPager.loadChapter(book, action.sectionId)
                            .catch { e ->
                                e.printStackTrace()
                            }
                            .collect { chapter ->
                            val page = chapter.toPage()
                            _state.update { it.copy(
                                pages = it.pages + (page.sectionId to page)) }
                        }
                    } finally {
                        sectionsLoading.remove(action.sectionId)
                        _state.update { it.copy(isLoading = false) }
                    }
                }
            }

            is BookDetailAction.PlaceBookmark -> {
                viewModelScope.launch {
                    val book = book() ?: return@launch
                    _state.update { it.copy(currentSection = action.sectionId, currentSectionOffset = action.scrollOffset) }
                    bookRepository.upsertBook(book.copy(currentSection = action.sectionId, currentSectionOffset = action.scrollOffset))
                }
            }

            BookDetailAction.ToggleReadingMode -> {
                val newMode = if (_state.value.readingMode == ReadingMode.IMMERSIVE)
                    ReadingMode.CONTROLS else ReadingMode.IMMERSIVE
                _state.update { it.copy(readingMode = newMode) }
            }
        }
    }

    private suspend fun writePages(book: Book) {
        _state.update { it.copy(isLoading = false, isWriting = true) }
        when (val result = bookPager.writePages(book.ISBN, book) { written, total ->
            _state.update { it.copy(
                writingProgress = written.toFloat() / total,
                writingSectionsWritten = written,
                writingSectionsTotal = total,
            ) }
        }) {
            is Result.Failure -> updateUiState(result.error)
            is Result.Success -> updateUiState(null)
        }
    }

    private fun updateUiState(error: BookParseError? = null) {
        _state.update {
            it.copy(
                error = error?.toUi(),
                isLoading = false,
                isWriting = false,
            )
        }
    }
}