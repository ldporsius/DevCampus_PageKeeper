package nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation

import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

data class MultiSelectState(
    val books: List<BookUi> = emptyList(),
    val selectedIsbn: Set<String> = emptySet(),
    val showDeleteConfirmation: Boolean = false,
    val bookPendingDelete: BookUi? = null
) {
    val selectedCount get() = selectedBooks.size
    val selectedBooks get() = books.filter { it.isbn in selectedIsbn }
}
