package nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation

import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

sealed interface MultiSelectEvent {
    data object NavigateBack : MultiSelectEvent
    data class ShareBooks(val books: List<BookUi>) : MultiSelectEvent
}
