package nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.codingwithlinda.pagekeeper.design_system.util.DeviceType
import nl.codingwithlinda.pagekeeper.design_system.util.Orientation
import nl.codingwithlinda.pagekeeper.design_system.util.rememberDeviceConfig
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.DeleteBookDialog
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.SelectionTopBar
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation.components.MultiSelectBookListItem

@Composable
fun MultiSelectScreen(
    state: MultiSelectState,
    onAction: (MultiSelectAction) -> Unit,
    onBookAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.showDeleteConfirmation) {
        DeleteBookDialog(
            bookTitle = "${state.selectedCount} book(s)",
            onConfirm = { onAction(MultiSelectAction.ConfirmDelete) },
            onDismiss = { onAction(MultiSelectAction.DismissDelete) }
        )
    }

    state.bookPendingDelete?.let { book ->
        DeleteBookDialog(
            bookTitle = book.title,
            onConfirm = { onBookAction(BookListItemAction.ConfirmDeleteClick) },
            onDismiss = { onBookAction(BookListItemAction.DismissDeleteClick) }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            SelectionTopBar(
                selectionCount = state.selectedCount,
                navigationIcon = R.drawable.back,
                onClear = { onAction(MultiSelectAction.NavigateBack) },
                onFavorite = { onAction(MultiSelectAction.AddToFavorites) },
                onShare = { onAction(MultiSelectAction.Share) },
                onDelete = { onAction(MultiSelectAction.Delete) }
            )
        }
    ) { paddingValues ->
        val deviceConfig = rememberDeviceConfig()
        val columns = if (
            deviceConfig.deviceType == DeviceType.Tablet ||
            deviceConfig.orientation == Orientation.Landscape
        ) 2 else 1

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(items = state.books, key = { it.isbn }) { book ->
                MultiSelectBookListItem(
                    book = book,
                    isSelected = book.isbn in state.selectedIsbn,
                    onToggle = { onAction(MultiSelectAction.ToggleBook(it)) },
                    onAction = onBookAction
                )
            }
        }
    }
}
