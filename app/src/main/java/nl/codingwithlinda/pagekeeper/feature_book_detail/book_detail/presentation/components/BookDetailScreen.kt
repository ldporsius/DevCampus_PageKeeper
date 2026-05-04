package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
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
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.toElementTextSpan

@OptIn(FlowPreview::class)
@Composable
fun BookDetailScreen(
    state: BookDetailState,
    readingSettings: ReadingSettings,
    onAction: (BookDetailAction) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
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

        val sortedPages = state.sortedPages()

       LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
        ) {
            sortedPages.forEach { page ->
                when (page) {
                    is Page.Loading -> item(key = "${page.sectionId}") {
                        LaunchedEffect(page.sectionId) {
                            //obsolete
                        }
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(800.dp),
                            contentAlignment = Alignment.Center
                        )
                        {
                            CircularProgressIndicator()
                        }
                    }
                   /* is ElementPage -> itemsIndexed(
                        items = page.elements,
                        key = { index, span -> "${page.sectionId}_$index" }
                    ) { _, element ->
                        element.toScaledText(readingSettings.fontSize)
                    }*/
                    is ElementPage ->{
                        page.elements.forEach { element ->
                            item(
                                key = element.element.id.toString()
                            ){
                                element.toScaledText(readingSettings.fontSize)
                            }
                        }
                    }

                    is Page.ImagePage -> item(key = "image_${page.sectionId}") {
                        AsyncImage(model = page.href, contentDescription = null)
                    }
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
                                element = Title(text = "The Great Gatsby"),
                                lines = listOf(FormattedLine(listOf(TextSpan(text = "The Great Gatsby"))))
                            ),
                            ElementTextSpan(
                                element = BookParagraph(text = "by F. Scott Fitzgerald"),
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
        )
    }
}