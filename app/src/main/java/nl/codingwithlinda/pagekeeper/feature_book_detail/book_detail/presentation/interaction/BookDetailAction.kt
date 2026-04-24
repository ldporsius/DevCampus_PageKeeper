package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction

sealed interface BookDetailAction {
    data object LoadNextSection : BookDetailAction

    data object ToggleReadingMode: BookDetailAction
}