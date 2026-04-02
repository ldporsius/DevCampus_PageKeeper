package nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation

import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

data class BookDetailState(
    val book: BookUi? = null,
    val isLoading: Boolean = true
)