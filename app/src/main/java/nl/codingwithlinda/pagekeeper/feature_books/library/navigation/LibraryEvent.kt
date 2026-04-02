package nl.codingwithlinda.pagekeeper.feature_books.library.navigation

sealed interface LibraryEvent {
    data class NavigateToDetail(val isbn: String) : LibraryEvent
}