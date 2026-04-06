package nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListScreen
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListState
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.components.EmptyFavoritesContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction

@Composable
fun FavoritesScreen(
    state: BookListState,
    onAction: (BookListItemAction) -> Unit,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BookListScreen(
        state = state,
        onAction = onAction,
        onBookClick = onBookClick,
        emptyContent = { EmptyFavoritesContent() },
        modifier = modifier
    )
}