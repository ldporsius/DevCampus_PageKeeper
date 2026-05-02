package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction

import androidx.compose.runtime.Stable
import nl.codingwithlinda.pagekeeper.core.presentation.util.UiText
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.Page
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingOrientation
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

@Stable
data class BookDetailState(
    val book: BookUi? = null,
    val pages: Map<Int, Page> = emptyMap(),
    val currentSection: Int = -1,
    val currentSectionOffset: Int = 0,
    val totalSections: Int = 1,
    val isWriting: Boolean = false,
    val writingProgress: Float = 0f,
    val writingSectionsWritten: Int = 0,
    val writingSectionsTotal: Int = 0,
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val readingMode: ReadingMode = ReadingMode.IMMERSIVE,
){
    fun sortedPages() = pages.values.sortedBy { it.sectionId }

    val paragraphs = pages.values.map {
        when(it){
            is Page.ElementPage -> it.elements
            else -> {
                emptyList()
            }
        }
    }.flatten()

}