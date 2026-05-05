package nl.codingwithlinda.pagekeeper.feature_book_detail.chapters

import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.ElementTextSpan

data class ChapterUiState(
    val totalChapters: Int,
    val chapters: List<ElementTextSpan>
)
