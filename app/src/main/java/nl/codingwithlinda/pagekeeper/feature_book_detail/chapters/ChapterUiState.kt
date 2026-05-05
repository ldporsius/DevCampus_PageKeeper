package nl.codingwithlinda.pagekeeper.feature_book_detail.chapters

import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.ElementTextSpan

data class ChapterItem(
    val sectionIndex: Int,
    val title: ElementTextSpan,
    val innerSections: List<ElementTextSpan>,
    val isExpanded: Boolean = false,
)

data class ChapterUiState(
    val totalChapters: Int,
    val chapters: List<ChapterItem>,
)