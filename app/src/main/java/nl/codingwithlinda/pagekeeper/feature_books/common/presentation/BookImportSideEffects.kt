package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.ImportFailedDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.UnsupportedFormatDialog
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookImportSideEffects(
    viewModel: LibraryViewModel = koinViewModel()
) {
    val libraryState = viewModel.state.collectAsStateWithLifecycle().value

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
}