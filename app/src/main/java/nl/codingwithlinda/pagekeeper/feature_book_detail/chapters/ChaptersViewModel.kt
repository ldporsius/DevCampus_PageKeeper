package nl.codingwithlinda.pagekeeper.feature_book_detail.chapters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookPager
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.InnerSection
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Section
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

    private val _currentSectionIndex = MutableStateFlow(0)


    val uiState = combine(_chapters, _expanded, _total, _bookTitle) { chapters, expanded, total, bookTitle ->
        val initChapters = List(total){ i ->
            i to null
        }.toMap()
        val sortedChapters = chapters.entries.sortedBy { it.key }.map { (index, data) ->
            ChapterItem(
                sectionIndex = index,
                title = data.title,
                innerSections = data.innerSections,
                isExpanded = index in expanded,
            )
        }
        val resultChapters = initChapters + sortedChapters.associateBy { it.sectionIndex }
        val isFlat = sortedChapters.isNotEmpty() && sortedChapters.all { it.innerSections.isEmpty() }
        ChapterUiState(
            totalChapters = total,
            bookTitle = bookTitle,
            isFlat = isFlat,
            chapters = resultChapters,
        )
    }.combine(_currentSectionIndex){ state, index ->
        println("--- ChaptersViewModel --- currentElementIndex = $index")

        state.copy(
            currentItemIndex = index
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChapterUiState(0, chapters = emptyMap()))

    private val sections = mutableSetOf<Int>()

    private val loadRange = MutableStateFlow(IntRange(0,0))

    val mutex = Mutex()
    val lock = Any()

    init {
        viewModelScope.launch {
            val total = bookPager.countPages(isbn)
            _total.update { total }

            bookRepository.getBookByISBN(isbn)?.let { book ->
                _bookTitle.value = Title(id = 0, text = book.title).toElementTextSpan()

                val range = book.currentSection - 5 until book.currentSection + 5

                for (i in range){
                    sections.add(i)
                    chapter(i)
                }
                _currentSectionIndex.update {
                    book.currentSection
                }
            }
        }

        loadRange.onEach { range ->
            mutex.withLock {
                for (i in range){
                    if (i in sections) continue
                    sections.add(i)
                    chapter(i)
                }
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun chapter(index: Int){
        bookPager.loadSection(isbn, index).collect { section ->
            val sectionData = createSectionData(section)
            _chapters.update { it + (section.id to sectionData) }
        }
    }

    private suspend fun createInnerSection(section: Section): InnerSection {
        val inner = InnerSection(id = section.id, elements = section.elements.drop(1))
        return inner
    }

    private suspend fun parseInnerSection(inner: InnerSection): SectionData {
        val titleElement = inner.elements.filterIsInstance<Title>().firstOrNull() ?: Title(id = inner.id, text = " ")
        val title = titleElement.toElementTextSpan()

        val innerSections = inner.elements.filterIsInstance<InnerSection>().map { inner ->
            val innerTitle = inner.elements.filterIsInstance<Title>().firstOrNull()
                ?: Title(id = inner.id, text = "---")
            innerTitle.toElementTextSpan()
        }

        return SectionData(title, innerSections)
    }

    private suspend fun createSectionData(section: Section): SectionData {
        val titleElement = section.elements.filterIsInstance<Title>().firstOrNull()

        if (titleElement == null) {
            println("--- ChaptersViewModel --- no title found for section ${section.id}")
            val inner = createInnerSection(section)
            return parseInnerSection(inner)
        }

        val title = titleElement.toElementTextSpan()

        val innerSections = section.elements.filterIsInstance<InnerSection>().map { inner ->
            val innerTitle = inner.elements.filterIsInstance<Title>().firstOrNull()
                ?: Title(id = inner.id, text = "---")
            innerTitle.toElementTextSpan()
        }
        return SectionData(title, innerSections)
    }


    fun loadChapters(firstIndex: Int, lastIndex: Int) = viewModelScope.launch {
        println("--- ChaptersViewModel --- loading chapters from $firstIndex to $lastIndex")
        val total = bookPager.countPages(isbn)

        val first = (firstIndex - 10).coerceAtLeast(0)
        val last = (lastIndex + 10).coerceAtMost(total)
        loadRange.update {
            IntRange(first, last)
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