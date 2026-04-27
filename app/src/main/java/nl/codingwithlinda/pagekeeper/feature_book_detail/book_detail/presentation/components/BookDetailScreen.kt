package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
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

@OptIn(FlowPreview::class)
@Composable
fun BookDetailScreen(
    state: BookDetailState,
    readingSettings: ReadingSettings,
    onAction: (BookDetailAction) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    scrollSettled: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .testTag("book_detail_screen")
    ) {
        if (state.isWriting) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                state.book?.title?.let { title ->
                    Text(
                        text = "Loading \"$title\"",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                LinearProgressIndicator(
                    progress = { state.writingProgress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loaded ${state.writingSectionsWritten} of ${state.writingSectionsTotal}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        state.error?.let { error ->
            BookParseErrorContent(
                error = error,
                modifier = Modifier.align(Alignment.Center)
            )
            return@Box
        }

        // Save reading position once the initial scroll has settled
        LaunchedEffect(listState, scrollSettled) {
            if (!scrollSettled) return@LaunchedEffect
            snapshotFlow {
                val firstItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
                val sectionId = firstItem?.key as? Int ?: -1
                val offset = listState.firstVisibleItemScrollOffset
                sectionId to offset
            }.debounce(500)
                .collect { (sectionId, offset) ->
                    if (sectionId != -1) {
                        onAction(BookDetailAction.PlaceBookmark(sectionId, offset))
                    }
                }
        }

        val sortedPages = remember(state.pages) { state.pages.values.sortedBy { it.sectionId } }

        LazyColumn(state = listState) {
            items(sortedPages, key = { page -> page.sectionId }) { page ->
                when (page) {
                    is Page.Loading -> {
                        LaunchedEffect(page.sectionId) {
                            onAction(BookDetailAction.LoadSection(page.sectionId))
                        }
                        Spacer(modifier = Modifier.height(400.dp))
                    }
                    is ElementPage -> page.toScaledText(readingSettings.fontSize)
                    is Page.ImagePage -> AsyncImage(
                        model = page.href,
                        contentDescription = null
                    )
                }
            }
        }

        if (state.isLoading) {
            val bg = Brush.verticalGradient(
                listOf(MaterialTheme.colorScheme.surfaceContainerLowest.copy(.5f),
                    MaterialTheme.colorScheme.surfaceContainerLowest)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(brush = bg)
                    .padding(vertical =  12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
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
                pages = mapOf(
                    0 to ElementPage(
                        sectionId = 0,
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
                isLoading = true
            ),
            readingSettings = ReadingSettings(),
            onAction = {},
            scrollSettled = true,
        )
    }
}