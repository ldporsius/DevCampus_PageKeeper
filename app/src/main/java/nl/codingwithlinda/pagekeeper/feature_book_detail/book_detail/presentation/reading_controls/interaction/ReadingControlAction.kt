package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction

sealed interface ReadingControlAction {
    data object ToggleAutoRotate: ReadingControlAction
    data class AdjustFontSize(val factor: Float): ReadingControlAction
    data class SetCurrentSection(val section: Int): ReadingControlAction
}