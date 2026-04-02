package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

data class LibraryState(
    val books: List<BookUi> = emptyList(),
    val isLoading: Boolean = false
)