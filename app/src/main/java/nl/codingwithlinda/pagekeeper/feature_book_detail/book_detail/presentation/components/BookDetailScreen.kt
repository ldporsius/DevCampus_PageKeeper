package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookParagraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettings
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.toScaledText
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailAction
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailState
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.ElementTextSpan
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.FormattedLine
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.Page
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.Page.ElementPage
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.TextSpan

@Composable
fun BookDetailScreen(
    state: BookDetailState,
    readingSettings: ReadingSettings,
    onAction: (BookDetailAction) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    Box(modifier = modifier
        .fillMaxSize()
        .safeContentPadding()
        .testTag("book_detail_screen")){
        if (state.isWriting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        state.error?.let { error ->
            BookParseErrorContent(
                error = error,
                modifier = Modifier.align(Alignment.Center)
            )
            return@Box
        }

        val nearBottom by remember {
            derivedStateOf {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
                val total = listState.layoutInfo.totalItemsCount
                total > 0 && lastVisible >= total - 2
            }
        }
        LaunchedEffect(nearBottom, state.isLoading) {
            if (nearBottom && !state.isLoading) onAction(BookDetailAction.LoadNextSection)
        }

        LazyColumn(state = listState) {
            itemsIndexed(
                state.pages,
                key = { _, page -> page.hashCode() }
            ) { index, page ->
                when (page) {
                    is ElementPage -> {
                       page.toScaledText(readingSettings.fontSize)
                    }
                    is Page.ImagePage -> {
                        AsyncImage(
                            model = page.href,
                            contentDescription = null
                        )
                    }
                }
            }
            if (state.isLoading){
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookDetailScreenPreview() {
    PageKeeperTheme {
        BookDetailScreen(
            state = BookDetailState(
                pages = listOf(
                    ElementPage(
                        elements = listOf(
                            ElementTextSpan(
                                element = Title("The Great Gatsby"),
                                lines = listOf(FormattedLine(listOf(TextSpan(text = "The Great Gatsby"))))
                            ),
                            ElementTextSpan(
                                element = BookParagraph("by F. Scott Fitzgerald"),
                                lines = listOf(FormattedLine(listOf(
                                    TextSpan(text = "by ", emphasis = true),
                                    TextSpan(text = "F. Scott Fitzgerald")
                                )))
                            ),
                        )
                    ),
                ),
                isLoading = false
            ),
            readingSettings = ReadingSettings(),
            onAction = {}
        )
    }
}