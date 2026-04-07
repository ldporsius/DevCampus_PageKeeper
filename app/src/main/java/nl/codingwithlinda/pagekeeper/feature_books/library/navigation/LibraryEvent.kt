package nl.codingwithlinda.pagekeeper.feature_books.library.navigation

import nl.codingwithlinda.pagekeeper.core.domain.model.Book

sealed interface LibraryEvent {
    data class NavigateToDetail(val isbn: String) : LibraryEvent
    data class ShowDuplicateDialog(val existing: Book, val incoming: Book) : LibraryEvent
}