package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

data class BookListState(
    val books: List<BookUi> = emptyList(),
    val bookPendingDelete: BookUi? = null
)