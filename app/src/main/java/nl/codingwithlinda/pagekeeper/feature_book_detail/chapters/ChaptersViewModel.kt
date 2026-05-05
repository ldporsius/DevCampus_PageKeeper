package nl.codingwithlinda.pagekeeper.feature_book_detail.chapters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookPager
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.InnerSection
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.ElementTextSpan
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.toElementTextSpan

class ChaptersViewModel(
    private val isbn: String,
    private val bookPager: BookPager,
    private val bookRepository: BookRepository,
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    private data class SectionData(
        val title: ElementTextSpan,
        val innerSections: List<ElementTextSpan>,
    )

    private val _chapters = MutableStateFlow<Map<Int, SectionData>>(emptyMap())
    private val _expanded = MutableStateFlow<Set<Int>>(emptySet())
    private val _total = MutableStateFlow(0)
    private val _bookTitle = MutableStateFlow<ElementTextSpan?>(null)

    val uiState = combine(_chapters, _expanded, _total, _bookTitle) { chapters, expanded, total, bookTitle ->
        val sortedChapters = chapters.entries.sortedBy { it.key }.map { (index, data) ->
            ChapterItem(
                sectionIndex = index,
                title = data.title,
                innerSections = data.innerSections,
                isExpanded = index in expanded,
            )
        }
        val isFlat = sortedChapters.isNotEmpty() && sortedChapters.all { it.innerSections.isEmpty() }
        ChapterUiState(
            totalChapters = total,
            bookTitle = bookTitle,
            isFlat = isFlat,
            chapters = sortedChapters,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChapterUiState(0, chapters = emptyList()))

    init {
        viewModelScope.launch {
            _total.value = bookPager.countPages(isbn)
            bookRepository.getBookByISBN(isbn)?.let { book ->
                _bookTitle.value = Title(id = 0, text = book.title).toElementTextSpan()
            }
        }
    }

    fun chapter(index: Int) = viewModelScope.launch {
        bookPager.loadSection(isbn, index).collect { section ->
            val titleElement = section.elements.filterIsInstance<Title>().firstOrNull()
                ?: Title(id = index, text = "--- no title ---")
            val title = titleElement.toElementTextSpan()

            val innerSections = section.elements.filterIsInstance<InnerSection>().map { inner ->
                val innerTitle = inner.elements.filterIsInstance<Title>().firstOrNull()
                    ?: Title(id = inner.id, text = "---")
                innerTitle.toElementTextSpan()
            }

            _chapters.update { it + (section.id to SectionData(title, innerSections)) }
        }
    }

    fun toggleExpand(sectionIndex: Int) {
        _expanded.update { current ->
            if (sectionIndex in current) current - sectionIndex else current + sectionIndex
        }
    }

    private val _navChannel = Channel<Boolean>()
    val navChannel = _navChannel.receiveAsFlow()

    fun updateCurrentSection(sectionIndex: Int, elementId: Int) = applicationScope.launch {
        val book = bookRepository.getBookByISBN(isbn) ?: return@launch
        bookRepository.upsertBook(book.copy(currentSection = sectionIndex, currentElementId = elementId))
        _navChannel.send(true)
    }
}