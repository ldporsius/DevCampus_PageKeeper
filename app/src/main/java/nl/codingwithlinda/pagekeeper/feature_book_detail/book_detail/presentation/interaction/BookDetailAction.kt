package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction

sealed interface BookDetailAction {
    data class LoadSection(val sectionId: Int) : BookDetailAction
    data class PlaceBookmark(val sectionId: Int) : BookDetailAction
    data object ToggleReadingMode : BookDetailAction
}