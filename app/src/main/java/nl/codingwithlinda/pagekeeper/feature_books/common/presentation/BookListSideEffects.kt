package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import java.io.File

@Composable
fun BookListSideEffects(viewModel: BookListViewModel) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.shareEvents) { book ->
        val file = File(context.filesDir, "${book.isbn}.fb2")
        val fileUri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TEXT, "I'm reading \"${book.title}\" by ${book.author}")
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    state.bookPendingDelete?.let { book ->
        DeleteBookDialog(
            bookTitle = book.title,
            onConfirm = { viewModel.onAction(BookListItemAction.ConfirmDeleteClick) },
            onDismiss = { viewModel.onAction(BookListItemAction.DismissDeleteClick) }
        )
    }
}
