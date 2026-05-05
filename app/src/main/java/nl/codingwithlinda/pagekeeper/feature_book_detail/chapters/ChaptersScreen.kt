package nl.codingwithlinda.pagekeeper.feature_book_detail.chapters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.toAnnotatedString
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.toTextStyle
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.ElementTextSpan

@Composable
fun ChaptersScreen(
    uiState: ChapterUiState,
    loadChapter: (Int) -> Unit,
    onToggleExpand: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    scaleFactor: Float,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Chapters")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "Back",
                        )
                    }
                }
            )
        }

    ) {innerPadding ->
    LazyColumn(modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()) {
        (0 until uiState.totalChapters).forEach { index ->
            item(key = "chapter_$index") {
                LaunchedEffect(index) { loadChapter(index) }

                val chapter = uiState.chapters.getOrNull(index)
                if (chapter == null) {
                    val empty = ElementTextSpan(
                        element = Title(id = index, text = "---"),
                    )
                    ChapterRow(title = empty, hasChildren = false, isExpanded = false, onClick = {})
                } else {
                    ChapterRow(
                        title = chapter.title,
                        hasChildren = chapter.innerSections.isNotEmpty(),
                        isExpanded = chapter.isExpanded,
                        onClick = { onToggleExpand(chapter.sectionIndex) },
                    )
                    AnimatedVisibility(visible = chapter.isExpanded) {
                        Column {
                            chapter.innerSections.forEach { innerTitle ->
                                InnerSectionRow(title = innerTitle)
                                HorizontalDivider(modifier = Modifier.padding(start = 32.dp))
                            }
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
}

@Composable
private fun ChapterRow(
    title: ElementTextSpan,
    hasChildren: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasChildren, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = title.toAnnotatedString(),
            style = title.element.toTextStyle(),
            modifier = Modifier.weight(1f),
        )
        if (hasChildren) {
            Icon(
                painter = painterResource(R.drawable.arrow_drop_down),
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.graphicsLayer {
                    rotationZ = if (isExpanded) 180f else 0f
                }
            )
        }
    }
}

@Composable
private fun InnerSectionRow(title: ElementTextSpan) {
    Text(
        text = title.toAnnotatedString(),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    )
}