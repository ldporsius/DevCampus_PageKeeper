package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction

sealed interface BookDetailAction {
    data class PlaceBookmark(val elementId: Int, val orientation: Int) : BookDetailAction
    data object ToggleReadingMode : BookDetailAction
}