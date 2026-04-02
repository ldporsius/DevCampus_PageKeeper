package nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction

sealed interface BookListItemAction {
    data class FavouriteClick(val isbn: String) : BookListItemAction
    data class ReadingClick(val isbn: String) : BookListItemAction
    data class ShareClick(val isbn: String) : BookListItemAction
    data class DeleteClick(val isbn: String) : BookListItemAction
}