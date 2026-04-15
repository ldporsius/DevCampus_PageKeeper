package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.navigation

sealed interface BookDetailEvent {
    data object NavigateBack : BookDetailEvent
}