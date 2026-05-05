package nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookImportSideEffects
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListSideEffects
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.components.BookItemsGrid
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.components.EmptyFavoritesContent
import nl.codingwithlinda.pagekeeper.feature_books.finished.presentation.components.EmptyFinishedContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components.EmptyLibraryContent
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun BooksPhoneLayout(
    onImportBook: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named("all")),
    libraryViewModel: LibraryViewModel = koinViewModel()
) {
    val state by bookListViewModel.state.collectAsStateWithLifecycle()
    val libraryState by libraryViewModel.state.collectAsStateWithLifecycle()

    val emptyContent: @Composable () -> Unit = {
        when (state.filter) {
            BookFilter.All -> EmptyLibraryContent() { onImportBook() }
            BookFilter.Favorites -> EmptyFavoritesContent()
            BookFilter.Finished -> EmptyFinishedContent()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BookImportSideEffects(onNavigateToDetail = onNavigateToDetail)
        BookListSideEffects(bookListViewModel)

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = state.books.isEmpty() && !state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                emptyContent()
            }
            AnimatedVisibility(
                visible = state.books.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BookItemsGrid(
                    books = state.books,
                    isImporting = libraryState.isImporting,
                    onCancelImport = { libraryViewModel.onAction(LibraryAction.CancelImport) },
                    onBookClick = { isbn -> onNavigateToDetail(isbn) },
                    onAction = bookListViewModel::onAction
                )
            }
        }

        libraryState.lastOpenedBookIsbn?.let { isbn ->
            FloatingActionButton(
                onClick = { onNavigateToDetail(isbn) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    painter = painterResource(R.drawable.book_vector),
                    contentDescription = "Resume reading",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}