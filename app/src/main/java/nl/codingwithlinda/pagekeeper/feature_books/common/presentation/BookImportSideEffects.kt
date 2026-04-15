package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.presentation.util.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.DuplicateBookDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.ImportFailedDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.UnsupportedFormatDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookImportSideEffects(
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: LibraryViewModel = koinViewModel()
) {
    val libraryState = viewModel.state.collectAsStateWithLifecycle().value
    var duplicateDialogData by remember { mutableStateOf<Pair<Book, Book>?>(null) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LibraryEvent.NavigateToDetail ->
                onNavigateToDetail(event.isbn)
            is LibraryEvent.ShowDuplicateDialog ->
                duplicateDialogData = event.existing to event.incoming
        }
    }

    if (libraryState.showUnsupportedFormatDialog) {
        UnsupportedFormatDialog(
            onDismiss = { viewModel.onAction(LibraryAction.DismissUnsupportedFormatDialog) }
        )
    }

    if (libraryState.importFailed) {
        ImportFailedDialog(
            onDismiss = { viewModel.onAction(LibraryAction.DismissImportFailed) }
        )
    }

    duplicateDialogData?.let { (existing, _) ->
        DuplicateBookDialog(
            existing = existing,
            onDismiss = {
                viewModel.onAction(LibraryAction.DismissDuplicateDialog)
                duplicateDialogData = null
            },
            onConfirm = {
                viewModel.onAction(LibraryAction.ConfirmOverwriteDuplicate)
                duplicateDialogData = null
            }
        )
    }
}