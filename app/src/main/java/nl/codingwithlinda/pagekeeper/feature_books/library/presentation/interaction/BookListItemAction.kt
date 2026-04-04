package nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction

sealed interface BookListItemAction {
    data class FavouriteClick(val isbn: String) : BookListItemAction
    data class FinishClick(val isbn: String) : BookListItemAction
    data class ShareClick(val isbn: String) : BookListItemAction
    data class DeleteClick(val isbn: String) : BookListItemAction
    data object ConfirmDeleteClick : BookListItemAction
    data object DismissDeleteClick : BookListItemAction
}