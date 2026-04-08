package nl.codingwithlinda.pagekeeper.feature_books.book_detail.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import nl.codingwithlinda.pagekeeper.core.domain.remote.Page
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_books.book_detail.navigation.BookDetailEvent
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BookDetailRoot(
    isbn: String,
    onNavigateBack: () -> Unit,
    viewModel: BookDetailViewModel = koinViewModel(key = isbn) { parametersOf(isbn) }
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
    Box(modifier = modifier.fillMaxSize().testTag("book_detail_screen")){
        LazyColumn() {
            items(state.pages){ page ->
                when (page) {
                    is Page.TextPage -> {
                        Column() {
                            page.lines.forEach { line ->
                                Text(text = line.toPlainText())
                            }
                        }
                    }
                    is Page.ImagePage -> {
                        val img = page.href
                        AsyncImage(
                            model = img,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}