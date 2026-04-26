package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction

sealed interface BookDetailAction {
    data object PageIsLoaded: BookDetailAction
    data object LoadNextSection : BookDetailAction
    data object LoadPreviousSection : BookDetailAction

    data class PlaceBookmark(val page: Int): BookDetailAction
    data object ToggleReadingMode: BookDetailAction
}