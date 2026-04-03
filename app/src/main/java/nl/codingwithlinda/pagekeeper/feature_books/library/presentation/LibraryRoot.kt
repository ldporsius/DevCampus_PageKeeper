package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListItem
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi
import nl.codingwithlinda.pagekeeper.feature_books.library.navigation.LibraryEvent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.EmptyLibraryContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun LibraryRoot(
    onNavigateToDetail: (String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named(BookFilter.All))
) {
    val books by bookListViewModel.books.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LibraryEvent.NavigateToDetail -> onNavigateToDetail(event.isbn)
        }
    }

    LibraryScreen(
        books = books,
        onAction = viewModel::onAction,
        onItemAction = bookListViewModel::onAction
    )
}

@Composable
fun LibraryScreen(
    books: List<BookUi>,
    onAction: (LibraryAction) -> Unit,
    onItemAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = books.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyLibraryContent(onImportBook = { onAction(LibraryAction.OnImportBookClick) })
        }

        AnimatedVisibility(
            visible = books.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = books, key = { it.isbn }) { book ->
                    BookListItem(
                        book = book,
                        onClick = { onAction(LibraryAction.OnBookClick(book.isbn)) },
                        onAction = onItemAction
                    )
                }
            }
        }
    }
}