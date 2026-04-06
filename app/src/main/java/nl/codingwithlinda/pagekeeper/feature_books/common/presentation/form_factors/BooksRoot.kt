package nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.presentation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.NavigationMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListSideEffects
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryScreen
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import nl.codingwithlinda.pagekeeper.navigation.BookDetailRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun BooksRoot(
    onImportBook: () -> Unit = {},
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named("all")),
    libraryViewModel: LibraryViewModel = koinViewModel()
) {
    val state by bookListViewModel.state.collectAsStateWithLifecycle()
    val libraryState by libraryViewModel.state.collectAsStateWithLifecycle()

    val controller = koinInject<MenuActionController>()
    val scope = rememberCoroutineScope()

    ObserveAsEvents(libraryViewModel.events) { event ->
        when (event) {
            is LibraryEvent.NavigateToDetail -> scope.launch {
                controller.onAction(NavigationMenuAction(BookDetailRoute(event.isbn)))
            }
        }
    }

    BookListSideEffects(bookListViewModel)

    LibraryScreen(
        state = state,
        libraryState = libraryState,
        isImporting = libraryState.isImporting,
        onImportBook = onImportBook,
        onCancelImport = { libraryViewModel.onAction(LibraryAction.CancelImport) },
        onLibraryAction = libraryViewModel::onAction,
        onAction = bookListViewModel::onAction
    )
}
