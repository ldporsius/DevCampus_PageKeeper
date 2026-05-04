package nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookImportSideEffects
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.components.BookItemsGrid
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListSideEffects
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.components.EmptyFavoritesContent
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.components.EmptyFinishedContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.EmptyLibraryContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import nl.codingwithlinda.pagekeeper.feature_books.search.SearchViewModel
import nl.codingwithlinda.pagekeeper.feature_books.search.components.EmptySearchComponent
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun BooksRootExpandedWidth(
    modifier: Modifier = Modifier,
    onImportBook: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named("all")),
    searchViewModel: SearchViewModel = koinViewModel<SearchViewModel>(),
    libraryViewModel: LibraryViewModel = koinViewModel()
) {

    val focusManager = LocalFocusManager.current
    var searchMode by remember { mutableStateOf(false) }
    var screenWidth by remember { mutableStateOf(1.dp) }
    val searchFieldWidth = animateFloatAsState(
        if (searchMode) 1f else  .5f
    )

    val searchIcon = if (searchMode) R.drawable.cancel else R.drawable.search

    val state by searchViewModel.state.collectAsStateWithLifecycle()


    val emptyContent: @Composable ()-> Unit = {
        when(state.filter){
            BookFilter.All -> EmptyLibraryContent() { onImportBook()}
            BookFilter.Favorites -> EmptyFavoritesContent()
            BookFilter.Finished -> EmptyFinishedContent()
        }
    }

    val emptySearch: @Composable ()-> Unit = { if (searchMode) EmptySearchComponent() else emptyContent() }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {

        BookListSideEffects(bookListViewModel)
        BookImportSideEffects(onNavigateToDetail = onNavigateToDetail)

        BoxWithConstraints (
            modifier = Modifier.fillMaxSize()
        ) {
            val mw = maxWidth

            Column(modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned{
                    screenWidth = mw
                }
            ) {
                TextField(
                    value = state.query,
                    onValueChange = searchViewModel::onQueryChange,
                    placeholder = { Text(stringResource(R.string.search_books_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(searchIcon),
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                if (searchMode) {
                                    searchMode = false
                                    focusManager.clearFocus()
                                    searchViewModel.onQueryChange("")
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .onFocusChanged { focusState ->
                            if (focusState.hasFocus) searchMode = true
                        }
                        .width(screenWidth * searchFieldWidth.value)
                        .align(Alignment.Start)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                    ,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        focusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )

                AnimatedContent(targetState =state.books.isEmpty() && !state.isLoading ) { empty ->
                    when (empty) {
                        true -> emptySearch()
                        false ->
                            BookItemsGrid(
                                books = state.books,
                                isImporting = libraryViewModel.state.collectAsStateWithLifecycle().value.isImporting,
                                onCancelImport = {
                                    libraryViewModel.onAction(LibraryAction.CancelImport)
                                },
                                onBookClick = { isbn -> onNavigateToDetail(isbn) },
                                onAction = bookListViewModel::onAction
                            )
                    }
                }
            }
        }
    }
}
