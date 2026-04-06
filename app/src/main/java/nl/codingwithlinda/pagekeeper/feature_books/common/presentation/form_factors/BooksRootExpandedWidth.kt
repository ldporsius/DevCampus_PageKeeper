package nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.NavigationMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListSideEffects
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryScreen
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import nl.codingwithlinda.pagekeeper.feature_books.search.SearchViewModel
import nl.codingwithlinda.pagekeeper.navigation.BookDetailRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

@Composable
fun BooksRootExpandedWidth(
    modifier: Modifier = Modifier,
    onImportBook: () -> Unit = {},
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named("all")),
    libraryViewModel: LibraryViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel(parameters = { parametersOf(BookFilter.All) }),
) {
    val state by bookListViewModel.state.collectAsStateWithLifecycle()
    val libraryState by libraryViewModel.state.collectAsStateWithLifecycle()
    val searchState by searchViewModel.state.collectAsStateWithLifecycle()

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

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TextField(
                value = searchState.query,
                onValueChange = searchViewModel::onQueryChange,
                placeholder = { Text(stringResource(R.string.search_books_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(50),
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .align(Alignment.Start)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )

            LibraryScreen(
                state = if (searchState.query.isBlank()) state else state.copy(books = searchState.books),
                libraryState = libraryState,
                isImporting = libraryState.isImporting,
                onImportBook = onImportBook,
                onCancelImport = { libraryViewModel.onAction(LibraryAction.CancelImport) },
                onLibraryAction = libraryViewModel::onAction,
                onAction = bookListViewModel::onAction
            )
        }
    }
}
