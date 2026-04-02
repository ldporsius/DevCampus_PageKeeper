package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun LibraryRoot(
    onNavigateToDetail: (String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LibraryEvent.NavigateToDetail -> onNavigateToDetail(event.isbn)
        }
    }

    LibraryScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun LibraryScreen(
    state: LibraryState,
    onAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.books.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyLibraryContent(onImportBook = { onAction(LibraryAction.OnImportBookClick) })
        }

        AnimatedVisibility(
            visible = state.books.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = state.books, key = { it.isbn }) { book ->
                    BookListItem(
                        book = book,
                        onAction = { action ->
                            when (action) {
                                is BookListItemAction.FavouriteClick ->
                                    onAction(LibraryAction.OnFavouriteClick(action.isbn))
                                is BookListItemAction.ReadingClick ->
                                    onAction(LibraryAction.OnReadingClick(action.isbn))
                                is BookListItemAction.ShareClick ->
                                    onAction(LibraryAction.OnShareClick(action.isbn))
                                is BookListItemAction.DeleteClick ->
                                    onAction(LibraryAction.OnDeleteClick(action.isbn))
                            }
                        }
                    )
                }
            }
        }
    }
}