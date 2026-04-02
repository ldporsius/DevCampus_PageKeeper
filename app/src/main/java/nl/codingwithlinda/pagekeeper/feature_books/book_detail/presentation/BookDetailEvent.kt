package nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation

sealed interface BookDetailEvent {
    data object NavigateBack : BookDetailEvent
}