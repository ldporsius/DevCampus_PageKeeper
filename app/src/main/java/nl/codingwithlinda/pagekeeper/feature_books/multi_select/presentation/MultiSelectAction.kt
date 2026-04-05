package nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation

sealed interface MultiSelectAction {
    data class ToggleBook(val isbn: String) : MultiSelectAction
    data object AddToFavorites : MultiSelectAction
    data object Share : MultiSelectAction
    data object Delete : MultiSelectAction
    data object ConfirmDelete : MultiSelectAction
    data object DismissDelete : MultiSelectAction
    data object NavigateBack : MultiSelectAction
}
