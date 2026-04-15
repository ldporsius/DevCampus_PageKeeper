package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.DeviceType
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.Orientation
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.rememberDeviceConfig
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction

@Composable
fun BookItemsGrid(
    modifier: Modifier = Modifier,
    books: List<BookUi>,
    isImporting: Boolean = false,
    onCancelImport: () -> Unit = {},
    onBookClick: (String) -> Unit = {},
    onAction: (BookListItemAction) -> Unit
) {
    val deviceConfig = rememberDeviceConfig()
    val columns = if (
        deviceConfig.deviceType == DeviceType.Tablet ||
        deviceConfig.orientation == Orientation.Landscape
    ) 2 else 1

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (isImporting) {
            item(key = "importing_placeholder", span = { GridItemSpan(1) }) {
                BookListItemPlaceholder(onCancel = onCancelImport)
            }
        }
        items(items = books, key = { it.isbn }) { book ->
            BookListItem(
                book = book,
                onClick = { onBookClick(book.isbn) },
                onAction = onAction
            )
        }
    }
}