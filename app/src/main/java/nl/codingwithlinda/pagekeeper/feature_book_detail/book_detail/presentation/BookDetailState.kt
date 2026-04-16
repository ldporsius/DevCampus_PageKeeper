package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation

import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data.PageElement
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Page
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

data class BookDetailState(
    val book: BookUi? = null,
    val pages: List<Page> = emptyList(),
    val isLoading: Boolean = true
)