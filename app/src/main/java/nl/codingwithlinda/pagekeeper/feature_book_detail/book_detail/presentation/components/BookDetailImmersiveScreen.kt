package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.room.util.TableInfo
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettings
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.FormFactorWrapper
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailState

@Composable
fun BookDetailImmersiveScreen(
    state: BookDetailState,
    readingSettings: ReadingSettings,
    listState: LazyListState,
    onTap: () -> Unit
) {
    FormFactorWrapper() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
        ) {
            BookDetailScreen(
                state = state,
                readingSettings = readingSettings,
                listState = listState,
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(true) {
                        detectTapGestures(
                            onTap = { onTap() }
                        )
                    }
            )

            val readingProgress = state.readingProgress

            LinearProgressIndicator(progress = { readingProgress }, modifier = Modifier.fillMaxWidth())
        }
    }
}