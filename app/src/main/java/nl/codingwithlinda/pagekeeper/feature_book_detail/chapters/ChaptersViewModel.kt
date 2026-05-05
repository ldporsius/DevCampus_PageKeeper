package nl.codingwithlinda.pagekeeper.feature_book_detail.chapters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookPager
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.PageElement
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.toElementTextSpan

class ChaptersViewModel(
    private val isbn: String,
    private val bookPager: BookPager,
): ViewModel() {

    private suspend fun  totalSections() = bookPager.countPages(isbn)

    private val _chapters = MutableStateFlow< Map<Int, PageElement>>(emptyMap())

    val uiState = _chapters.map { chapters ->
        val total = totalSections()
        val list = chapters.values.sortedBy { it.id }.map { it.toElementTextSpan() }
        ChapterUiState(total, list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChapterUiState(0, emptyList()))

    fun chapter(index: Int) = viewModelScope.launch {
        bookPager.loadSection(isbn, index).collect { section ->
            val title = section.elements.filterIsInstance<Title>().firstOrNull() ?: Title(id = index, text = "--- no title ---")
            _chapters.update {
                it + (section.id to title)
            }
        }
    }

}