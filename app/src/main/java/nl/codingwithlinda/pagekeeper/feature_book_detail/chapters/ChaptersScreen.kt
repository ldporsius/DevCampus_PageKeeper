package nl.codingwithlinda.pagekeeper.feature_book_detail.chapters

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.toScaledText

@Composable
fun ChaptersScreen(
    uiState: ChapterUiState,
    loadChapter: (Int) -> Unit,
    scaleFactor: Float,
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        (0 until uiState.totalChapters).forEach {
            item(key = "$it"){
                LaunchedEffect(it) {
                    loadChapter(it)
                }
                Box(modifier = Modifier.height(100.dp)) {
                    val text = uiState.chapters.getOrNull(it)
                    if (text == null){
                        Text("loading")
                    }
                    else{
                        text?.toScaledText(scaleFactor)
                    }
                }
            }
        }
    }

}