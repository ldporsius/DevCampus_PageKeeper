package nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.interaction

import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

data class FinishedState(
    val books: List<BookUi> = emptyList()
)