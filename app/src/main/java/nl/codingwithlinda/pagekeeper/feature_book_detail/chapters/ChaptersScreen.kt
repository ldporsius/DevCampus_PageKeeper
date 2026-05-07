package nl.codingwithlinda.pagekeeper.feature_book_detail.chapters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.toAnnotatedString
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.toTextStyle
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.ElementTextSpan

@Composable
fun ChaptersScreen(
    uiState: ChapterUiState,
    loadChapter: (Int, Int) -> Unit,
    onToggleExpand: (Int) -> Unit,
    onItemClick: (sectionIndex: Int, elementId: Int) -> Unit,
    onNavigateBack: () -> Unit,
    scaleFactor: Float,
) {
    val listState = rememberLazyListState()

    var scollSettled = false
    val hasScrolled = remember(listState, uiState.currentItemIndex) {
        derivedStateOf { listState.firstVisibleItemIndex != uiState.currentItemIndex }
    }
    LaunchedEffect(uiState.currentItemIndex) {
        if (uiState.currentItemIndex == 0 || scollSettled) return@LaunchedEffect
        listState.scrollToItem(uiState.currentItemIndex)
        scollSettled = true
    }
    LaunchedEffect(hasScrolled) {
        if (!scollSettled) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.visibleItemsInfo}
            .collect { info ->
                val firstVisible = info.firstOrNull()?.index ?: 0
                val lastVisible = info.lastOrNull()?.index ?: 0
                loadChapter(firstVisible, lastVisible)
            }
    }
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

    ) { innerPadding ->
        val chaptersByIndex = uiState.chapters.values.toList()
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            ,
            contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(600.dp)
            ) {

                items(uiState.chapters.size,
                    key = { index ->
                        index
                    }
                ){ index ->
                    val chapterItem = chaptersByIndex.getOrNull(index)
                    if (chapterItem == null) {
                        Text("...")
                    }
                    chapterItem?.let { ch ->
                        ChapterRow(
                            isCurrentChapter = uiState.isCurrentChapter(ch.sectionIndex),
                            title = ch.title,
                            hasChildren = ch.innerSections.isNotEmpty(),
                            isExpanded = ch.isExpanded,
                            onClick = { onToggleExpand(ch.sectionIndex) },
                        )

                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterRow(
    isCurrentChapter: Boolean,
    title: ElementTextSpan,
    hasChildren: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {

    val textStyle = if (isCurrentChapter) {
        title.element.toTextStyle()
    }else{
        MaterialTheme.typography.bodyMedium
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasChildren, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = title.toAnnotatedString(),
            style = textStyle,
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
private fun InnerSectionRow(
    title: ElementTextSpan,
    onClick: () -> Unit,
) {
    Text(
        text = title.toAnnotatedString(),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    )
}