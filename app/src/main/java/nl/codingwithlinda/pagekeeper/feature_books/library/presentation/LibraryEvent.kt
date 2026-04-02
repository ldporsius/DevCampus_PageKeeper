package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

sealed interface LibraryEvent {
    data class NavigateToDetail(val isbn: String) : LibraryEvent
}