package nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction

import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

data class LibraryState(
    val books: List<BookUi> = emptyList(),
    val isLoading: Boolean = false
)