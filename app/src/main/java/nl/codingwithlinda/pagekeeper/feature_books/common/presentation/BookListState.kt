package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

data class BookListState(
    val books: List<BookUi> = emptyList(),
    val bookPendingDelete: BookUi? = null,
    val isLoading: Boolean = true,
    val filter: BookFilter = BookFilter.All
)