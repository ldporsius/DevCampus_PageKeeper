package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction

sealed interface BookDetailAction {
    data class PlaceBookmark(val sectionId: Int, val scrollOffset: Int = 0, val orientation: Int) : BookDetailAction
    data object ToggleReadingMode : BookDetailAction
}