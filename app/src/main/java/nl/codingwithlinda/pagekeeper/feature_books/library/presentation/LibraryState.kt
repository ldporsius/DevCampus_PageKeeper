package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

data class LibraryState(
    val showUnsupportedFormatDialog: Boolean = false,
    val isImporting: Boolean = false,
    val importFailed: Boolean = false,
    val lastOpenedBook: BookUi? = null,
)