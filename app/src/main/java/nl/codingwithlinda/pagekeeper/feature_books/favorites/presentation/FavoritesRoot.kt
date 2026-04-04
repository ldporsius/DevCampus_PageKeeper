package nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation

import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListItem
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListState
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.DeleteBookDialog
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.components.EmptyFavoritesContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun FavoritesRoot(
    viewModel: BookListViewModel = koinViewModel(qualifier = named(BookFilter.Favorites))
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.shareEvents) { book ->
        val file = File(context.filesDir, "${book.isbn}.fb2")
        val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TEXT, "I'm reading \"${book.title}\" by ${book.author}")
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    FavoritesScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun FavoritesScreen(
    state: BookListState,
    onAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
    state.bookPendingDelete?.let { book ->
        DeleteBookDialog(
            bookTitle = book.title,
            onConfirm = { onAction(BookListItemAction.ConfirmDeleteClick) },
            onDismiss = { onAction(BookListItemAction.DismissDeleteClick) }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.books.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyFavoritesContent()
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
                        onAction = onAction
                    )
                }
            }
        }
    }
}