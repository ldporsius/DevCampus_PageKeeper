package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

sealed interface BookListItemAction {
    data object FavouriteClick : BookListItemAction
    data object ReadingClick : BookListItemAction
    data object ShareClick : BookListItemAction
    data object DeleteClick : BookListItemAction
}