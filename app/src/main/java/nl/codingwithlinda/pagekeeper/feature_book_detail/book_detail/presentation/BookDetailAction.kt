package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation

sealed interface BookDetailAction {
    data object OnBackClick : BookDetailAction
}