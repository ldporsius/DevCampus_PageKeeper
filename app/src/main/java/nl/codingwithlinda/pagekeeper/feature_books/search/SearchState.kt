package nl.codingwithlinda.pagekeeper.feature_books.search

import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

data class SearchState(
    val isLoading: Boolean = true,
    val query: String = "",
    val filter: BookFilter = BookFilter.All,
    val books: List<BookUi> = emptyList(),
    val bookPendingDelete: BookUi? = null,
)