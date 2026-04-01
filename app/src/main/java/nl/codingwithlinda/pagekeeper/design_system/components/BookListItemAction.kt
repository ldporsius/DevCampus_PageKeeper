package nl.codingwithlinda.pagekeeper.design_system.components

sealed interface BookListItemAction {
    data object FavouriteClick : BookListItemAction
    data object ReadingClick : BookListItemAction
    data object ShareClick : BookListItemAction
    data object DeleteClick : BookListItemAction
}