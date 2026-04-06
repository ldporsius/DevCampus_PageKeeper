package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction

@Composable
fun BookListScreen(
    state: BookListState,
    onAction: (BookListItemAction) -> Unit,
    onBookClick: (String) -> Unit,
    emptyContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpandedWidth = currentWindowAdaptiveInfo()
        .windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    val content: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = state.books.isEmpty() && !state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                emptyContent()
            }
            AnimatedVisibility(
                visible = state.books.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BookItemsGrid(
                    books = state.books,
                    onBookClick = onBookClick,
                    onAction = onAction
                )
            }
        }
    }

    if (isExpandedWidth) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            content()
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            content()
        }
    }
}