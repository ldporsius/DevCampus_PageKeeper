package nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction

sealed interface LibraryAction {
    data class OnBookClick(val isbn: String) : LibraryAction
    data class OnImportBookClick(val uri: String) : LibraryAction

}