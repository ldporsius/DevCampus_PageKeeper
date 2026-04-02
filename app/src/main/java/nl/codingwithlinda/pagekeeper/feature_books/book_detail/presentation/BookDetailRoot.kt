package nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.navigation.BookDetailEvent
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BookDetailRoot(
    isbn: String,
    onNavigateBack: () -> Unit,
    viewModel: BookDetailViewModel = koinViewModel { parametersOf(isbn) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is BookDetailEvent.NavigateBack -> onNavigateBack()
        }
    }

    BookDetailScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun BookDetailScreen(
    state: BookDetailState,
    onAction: (BookDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Implement detail UI
    Box(modifier = modifier.fillMaxSize())
}