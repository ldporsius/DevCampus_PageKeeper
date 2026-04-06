package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.design_system.util.DeviceType
import nl.codingwithlinda.pagekeeper.design_system.util.Orientation
import nl.codingwithlinda.pagekeeper.design_system.util.rememberDeviceConfig
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListItem
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListItemPlaceholder
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListState
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.EmptyLibraryContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.ImportFailedDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.UnsupportedFormatDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction

@Composable
fun LibraryScreen(
    state: BookListState,
    libraryState: LibraryState,
    isImporting: Boolean,
    onImportBook: () -> Unit,
    onCancelImport: () -> Unit,
    onLibraryAction: (LibraryAction) -> Unit,
    onAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {


    if (libraryState.showUnsupportedFormatDialog) {
        UnsupportedFormatDialog(
            onDismiss = { onLibraryAction(LibraryAction.DismissUnsupportedFormatDialog) }
        )
    }

    if (libraryState.importFailed) {
        ImportFailedDialog(
            onDismiss = { onLibraryAction(LibraryAction.DismissImportFailed) }
        )
    }
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.books.isEmpty() && !isImporting && !state.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyLibraryContent(onImportBook = onImportBook)
        }

        AnimatedVisibility(
            visible = state.books.isNotEmpty() || isImporting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val deviceConfig = rememberDeviceConfig()
            val columns = if (
                deviceConfig.deviceType == DeviceType.Tablet ||
                deviceConfig.orientation == Orientation.Landscape
            ) 2 else 1

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (isImporting) {
                    item(key = "importing_placeholder", span = { GridItemSpan(maxLineSpan) }) {
                        BookListItemPlaceholder(onCancel = onCancelImport)
                    }
                }
                items(items = state.books, key = { it.isbn }) { book ->
                    BookListItem(
                        book = book,
                        onClick = { onLibraryAction(LibraryAction.OnBookClick(book.isbn)) },
                        onAction = onAction
                    )
                }
            }
        }
    }
}