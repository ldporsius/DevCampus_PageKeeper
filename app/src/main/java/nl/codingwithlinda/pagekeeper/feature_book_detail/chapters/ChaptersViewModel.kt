package nl.codingwithlinda.pagekeeper.feature_book_detail.chapters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookPager
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.InnerSection
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.ElementTextSpan
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.toElementTextSpan

class ChaptersViewModel(
    private val isbn: String,
    private val bookPager: BookPager,
) : ViewModel() {

    private data class SectionData(
        val title: ElementTextSpan,
        val innerSections: List<ElementTextSpan>,
    )

    private val _chapters = MutableStateFlow<Map<Int, SectionData>>(emptyMap())
    private val _expanded = MutableStateFlow<Set<Int>>(emptySet())
    private val _total = MutableStateFlow(0)

    val uiState = combine(_chapters, _expanded, _total) { chapters, expanded, total ->
        ChapterUiState(
            totalChapters = total,
            chapters = chapters.entries.sortedBy { it.key }.map { (index, data) ->
                ChapterItem(
                    sectionIndex = index,
                    title = data.title,
                    innerSections = data.innerSections,
                    isExpanded = index in expanded,
                )
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChapterUiState(0, emptyList()))

    init {
        viewModelScope.launch {
            _total.value = bookPager.countPages(isbn)
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
}