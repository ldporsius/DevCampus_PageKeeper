package nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation

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
import nl.codingwithlinda.pagekeeper.core.util.toShareFile
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun MultiSelectRoot(
    filter: BookFilter,
    onNavigateBack: () -> Unit
) {
    val viewModel: MultiSelectViewModel = koinViewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(filter) {
        viewModel.savedStateHandle[MultiSelectViewModel.KEY_FILTER] = filter
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.events, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    MultiSelectEvent.NavigateBack -> onNavigateBack()

                    is MultiSelectEvent.ShareBooks -> {
                        val uris = withContext(Dispatchers.IO) {
                            event.books.mapNotNull { book ->
                                val source = File(context.filesDir, "${book.isbn}.fb2")
                                val shareFile = source.toShareFile(context.cacheDir, book.title)
                                runCatching {
                                    FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        shareFile
                                    )
                                }.getOrNull()
                            }
                        }
                        val titles = event.books.joinToString("\n") { "\"${it.title}\" by ${it.author}" }
                        val intent = if (uris.size == 1) {
                            Intent(Intent.ACTION_SEND).apply {
                                type = "application/octet-stream"
                                putExtra(Intent.EXTRA_TEXT, titles)
                                putExtra(Intent.EXTRA_STREAM, uris.first())
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        } else {
                            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = "application/octet-stream"
                                putExtra(Intent.EXTRA_TEXT, titles)
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    }
                }
            }
        }
    }

    MultiSelectScreen(
        state = state,
        onAction = viewModel::onAction,
        onBookAction = viewModel::onBookAction
    )
}