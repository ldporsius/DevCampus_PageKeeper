package nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction

sealed interface LibraryAction {
    data object OnImportBookClick : LibraryAction
    data class OnBookClick(val isbn: String) : LibraryAction
}