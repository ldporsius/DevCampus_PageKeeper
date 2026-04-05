package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

data class LibraryState(
    val showUnsupportedFormatDialog: Boolean = false,
    val isImporting: Boolean = false,
    val importFailed: Boolean = false
)