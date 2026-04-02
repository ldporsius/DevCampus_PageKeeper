package nl.codingwithlinda.pagekeeper.feature_books.book_detail.navigation

sealed interface BookDetailEvent {
    data object NavigateBack : BookDetailEvent
}