package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.codingwithlinda.pagekeeper.core.data.util.toShareFile
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import java.io.File

@Composable
fun BookListSideEffects(viewModel: BookListViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.shareEvents, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.shareEvents.collect { book ->
                val fileUri = withContext(Dispatchers.IO) {
                    val source = File(context.filesDir, "${book.isbn}.fb2")
                    val shareFile = source.toShareFile(context.cacheDir, book.title)
                    runCatching {
                        FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", shareFile
                        )
                    }.getOrNull()
                } ?: return@collect
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_TEXT, "I'm reading \"${book.title}\" by ${book.author}")
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, null))
            }
        }
    }

    state.bookPendingDelete?.let { book ->
        DeleteBookDialog(
            bookTitle = book.title,
            onConfirm = { viewModel.onAction(BookListItemAction.ConfirmDeleteClick) },
            onDismiss = { viewModel.onAction(BookListItemAction.DismissDeleteClick) }
        )
    }
}
