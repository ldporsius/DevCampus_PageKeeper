package nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction

sealed interface LibraryAction {
    data object OnImportBookClick : LibraryAction
    data class OnBookClick(val isbn: String) : LibraryAction
    data class OnFavouriteClick(val isbn: String) : LibraryAction
    data class OnReadingClick(val isbn: String) : LibraryAction
    data class OnShareClick(val isbn: String) : LibraryAction
    data class OnDeleteClick(val isbn: String) : LibraryAction
}