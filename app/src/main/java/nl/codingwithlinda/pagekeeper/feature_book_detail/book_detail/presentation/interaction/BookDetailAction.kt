package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction

import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.Orientation

sealed interface BookDetailAction {
    data class LoadSection(val sectionId: Int) : BookDetailAction
    data class PlaceBookmark(val sectionId: Int, val scrollOffset: Int = 0, val orientation: Int) : BookDetailAction
    data object ToggleReadingMode : BookDetailAction
}