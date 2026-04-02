package nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation

sealed interface BookDetailAction {
    data object OnBackClick : BookDetailAction
}