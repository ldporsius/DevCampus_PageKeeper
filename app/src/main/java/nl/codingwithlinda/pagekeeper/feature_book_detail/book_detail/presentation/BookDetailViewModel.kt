package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import nl.codingwithlinda.pagekeeper.core.domain.util.map
import nl.codingwithlinda.pagekeeper.core.domain.util.onFailure
import nl.codingwithlinda.pagekeeper.core.domain.util.onSuccess
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookPager
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookParseError
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.PageElement
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Section
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.util.Paginator
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailAction
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailState
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.ReadingMode
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.Page
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.toPage
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.util.toUi
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.toBookUi
import kotlin.math.abs

class BookDetailViewModel(
    private val isbn: String,
    private val bookRepository: BookRepository,
    private val bookPager: BookPager
) : ViewModel() {

    private val _state = MutableStateFlow(BookDetailState())
    val state = _state
        .onEach {
            viewModelScope.launch {
                val pageIndices =
                    it.pages.values.filter { it !is Page.Loading }.map { it.sectionId }
                println("---BOOK DETAIL VIEW MODEL --- PAGES loaded: ${pageIndices.joinToString()}. Total: ${it.pages.count { (_, p) -> p !is Page.Loading }} / ${it.pages.size}")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    private val evictionWindow = 3

    private suspend fun book() = bookRepository.getBookByISBN(isbn)

    private val book = bookRepository.observeBook(isbn).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    val listState = LazyListState()

    init {
        viewModelScope.launch {
            book.collect { book ->
                if (book == null) return@collect
                _state.update { it.copy(book = book.toBookUi()) }
            }
        }

        viewModelScope.launch {
            val book = book() ?: return@launch

            if (!bookPager.hasPages(isbn) || isLegacyPages()) {
                writePages()
            }

            val totalSections = bookPager.countPages(isbn)
            val initialSection = book.currentSection
            val initialElementId = book.currentElementId

            val evictionFirst = (initialSection - evictionWindow).coerceAtLeast(0)
            val evictionLast = (initialSection + evictionWindow).coerceAtMost(totalSections)
            val loadingPages = (evictionFirst until evictionLast).associateWith { i -> Page.Loading(i) }

            val initPages = mutableMapOf<Int, Page>()
            loadingPages.onEach {
                bookPager.loadSection(isbn, it.key)
                    .collect { section ->
                        initPages += (section.id to section.toPage())
                    }
            }
            _state.update {state ->
                state.copy(
                    book = book.toBookUi(),
                    pages = initPages,
                    totalSections = totalSections,
                    currentSection = initialSection,
                    currentElementId = initialElementId,
                    isLoading = false,
                )
            }
        }
    }

    fun onAction(action: BookDetailAction) {
        when (action) {

            is BookDetailAction.PlaceBookmark -> {
                viewModelScope.launch {
                    val book = book() ?: return@launch
                    val sectionId = state.value.elementPages.firstOrNull { page ->
                        page.elements.any { it.element.id == action.elementId }
                    }?.sectionId ?: return@launch
                    println("---BOOK DETAIL VIEW MODEL --- BOOKMARKED elementId ${action.elementId} in section $sectionId, orientation ${action.orientation}")
                    _state.update { it.copy(currentSection = sectionId, currentElementId = action.elementId) }
                    loadSections(sectionId)
                    bookRepository.upsertBook(book.copy(currentSection = sectionId, currentElementId = action.elementId))
                }
            }

            BookDetailAction.ToggleReadingMode -> {
                val newMode = if (_state.value.readingMode == ReadingMode.IMMERSIVE)
                    ReadingMode.CONTROLS else ReadingMode.IMMERSIVE
                _state.update { it.copy(readingMode = newMode) }
            }
        }
    }

    // Pages written before per-element ids existed have all leaf ids == 0.
    // If we see >1 leaf element all sharing id 0, treat as legacy and reparse.
    private suspend fun isLegacyPages(): Boolean {
        val sections = (bookPager.loadSections(isbn, 0) as? Result.Success)?.data ?: return false
        val leafIds = sections.flatMap { it.leafIds() }
        return leafIds.size > 1 && leafIds.all { it == 0 }
    }

    private fun Section.leafIds(): List<Int> = elements.flatMap {
        if (it is Section) it.leafIds() else listOf(it.id)
    }

    private suspend fun writePages() {
        _state.update { it.copy(isLoading = false, isWriting = true) }
        when (val result = bookPager.writePages(isbn, isbn) { written, total ->
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

    private fun loadSections(anchorSection: Int) = viewModelScope.launch{
        val total = bookPager.countPages(isbn)
        val first = (anchorSection - evictionWindow).coerceAtLeast(0)
        val last = (state.value.elementPages.last().sectionId + evictionWindow).coerceAtMost(total)
        val range = first until last
        println("--- LOAD SECTIONS --- range: $range, total: $total")
        for (i in range) {
            if (i in state.value.elementPages.map { it.sectionId }) continue
            bookPager.loadSection(isbn, i).collect { section ->
                _state.update { state ->
                    state.copy(pages = state.pages + (section.id to section.toPage()))
                }
            }
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