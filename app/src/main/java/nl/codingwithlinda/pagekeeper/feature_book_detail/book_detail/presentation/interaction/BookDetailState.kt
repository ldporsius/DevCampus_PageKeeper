package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction

import nl.codingwithlinda.pagekeeper.core.presentation.util.UiText
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Page
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingOrientation
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

data class BookDetailState(
    val book: BookUi? = null,
    val pages: List<Page> = emptyList(),
    val isWriting: Boolean = false,
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val readingMode: ReadingMode = ReadingMode.IMMERSIVE,
    val orientation: ReadingOrientation = ReadingOrientation.AUTO_ROTATE
)