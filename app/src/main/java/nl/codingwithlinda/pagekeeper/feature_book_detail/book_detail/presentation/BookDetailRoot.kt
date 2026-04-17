package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Page
import nl.codingwithlinda.pagekeeper.core.presentation.util.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.navigation.BookDetailEvent
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
    Box(modifier = modifier
        .fillMaxSize()
        .safeContentPadding()
        .testTag("book_detail_screen")){
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        LazyColumn() {
            items(state.pages){ page ->
                when (page) {
                    is Page.TextPage -> {
                        Column() {
                            page.lines.forEach { line ->
                                Text(text = buildAnnotatedString {
                                    line.spans.forEach { span ->
                                        when {
                                            span.url != null -> withLink(LinkAnnotation.Url(span.url)) {
                                                append(span.text)
                                            }
                                            span.emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                                append(span.text)
                                            }
                                            span.bold -> withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                                append(span.text)
                                            }
                                            else -> append(span.text)
                                        }
                                    }
                                })
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