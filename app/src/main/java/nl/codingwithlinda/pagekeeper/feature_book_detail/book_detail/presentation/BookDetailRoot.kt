package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.debounce
import nl.codingwithlinda.pagekeeper.core.domain.util.Logger
import nl.codingwithlinda.pagekeeper.core.presentation.util.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components.BookDetailImmersiveScreen
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components.BookDetailScaffold
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailAction
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.ReadingMode
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingControlsViewModel
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingOrientation
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(kotlinx.coroutines.FlowPreview::class)
@Composable
fun BookDetailRoot(
    isbn: String,
    onNavigateBack: () -> Unit,
    navToChapters: () -> Unit,
    viewModel: BookDetailViewModel = koinViewModel(key = isbn) { parametersOf(isbn) },
    readingControlsViewModel: ReadingControlsViewModel = koinViewModel(),
    logger: Logger,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current

    val readingSettings by readingControlsViewModel.state.collectAsStateWithLifecycle()

    val activity = LocalActivity.current

    activity?.let{
        it.requestedOrientation = when(readingSettings.orientation){
            ReadingOrientation.AUTO_ROTATE -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            ReadingOrientation.LOCKED_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        }
    }

    val listState = viewModel.listState

    var scrollSettled by rememberSaveable(state.book?.formattedDate) { mutableStateOf(false) }

    LaunchedEffect(scrollSettled, state.elementPages) {
        if (scrollSettled || state.elementPages.isEmpty()) return@LaunchedEffect
        val targetIndex = state.elementPages
            .flatMap { it.elements }
            .indexOfFirst { it.element.id == state.currentElementId }
        if (targetIndex < 0) return@LaunchedEffect
        logger.log("--- BOOK DETAIL --- SCROLLING TO ELEMENT ${state.currentElementId} at index $targetIndex")
        listState.scrollToItem(index = targetIndex)
        scrollSettled = true
    }


    val hasScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex != state.currentElementId }
    }

    LaunchedEffect(hasScrolled) {
        snapshotFlow {
            val firstItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            (firstItem?.key as? String)?.toIntOrNull() ?: -1
        }.debounce(500)
            .collect { elementId ->
                logger.log("--- BOOK DETAIL --- firstItem. elementId = $elementId")

                if (elementId != -1) {
                    val orientation = configuration.orientation
                    viewModel.onAction(BookDetailAction.PlaceBookmark(elementId, orientation))
                }
            }
    }

    when(state.readingMode){
        ReadingMode.IMMERSIVE -> {
            BookDetailImmersiveScreen(
                state = state,
                readingSettings = readingSettings,
                listState = listState,
                onTap = { viewModel.onAction(BookDetailAction.ToggleReadingMode) }
            )
        }
        ReadingMode.CONTROLS -> {
            state.book?.let { book ->
                BookDetailScaffold(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    listState = listState,
                    readingSettings = readingSettings,
                    onAction = readingControlsViewModel::onAction,
                    toggleReadingMode = { viewModel.onAction(BookDetailAction.ToggleReadingMode) },
                    onNavBack = onNavigateBack,
                )
            }
        }
    }

    ObserveAsEvents(readingControlsViewModel.navToChapters) {
        navToChapters()
    }

}

