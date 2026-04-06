package nl.codingwithlinda.pagekeeper.feature_books.search

import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

data class SearchState(
    val query: String = "",
    val books: List<BookUi> = emptyList()
)