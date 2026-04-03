package nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.interaction

import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

data class FavoritesState(
    val books: List<BookUi> = emptyList()
)