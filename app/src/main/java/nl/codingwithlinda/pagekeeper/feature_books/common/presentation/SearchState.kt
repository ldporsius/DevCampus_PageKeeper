package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

data class SearchState(
    val query: String = "",
    val books: List<BookUi> = emptyList()
)
