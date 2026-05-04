package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.debounce
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.rememberDeviceConfig
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettings
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components.BookDetailImmersiveScreen
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components.BookDetailScaffold
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components.BookDetailScreen
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components.FontSizeIndicator
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components.PaginationScrollListener
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.FormFactorWrapper
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.toReadingControls
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.typographySliderRange
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailAction
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailState
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.ReadingMode
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ControlsRow
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.FontSizeSlider
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingControlsAdaptive
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingControlsViewModel
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingOrientation
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.AutoRotateControl
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.FontSizeControl
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControl
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControlAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(kotlinx.coroutines.FlowPreview::class)
@Composable
fun BookDetailRoot(
    isbn: String,
    onNavigateBack: () -> Unit,
    viewModel: BookDetailViewModel = koinViewModel(key = isbn) { parametersOf(isbn) },
    readingControlsViewModel: ReadingControlsViewModel = koinViewModel(),
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
        println("--- BOOK DETAIL --- SCROLLING TO ELEMENT ${state.currentElementId} at index $targetIndex")
        listState.scrollToItem(index = targetIndex)
        scrollSettled = true
    }


    val hasScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex != state.currentElementId }
    }
    // Save reading position once the initial scroll has settled
    LaunchedEffect(hasScrolled) {
        if (!hasScrolled) return@LaunchedEffect
        snapshotFlow {
            val firstItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            (firstItem?.key as? String)?.toIntOrNull() ?: -1
        }.debounce(500)
            .collect { elementId ->
                if (elementId != -1) {
                    println("--- BOOK DETAIL --- firstItem. elementId = $elementId")
                    val orientation = configuration.orientation
                    viewModel.onAction(BookDetailAction.PlaceBookmark(elementId, orientation))
                }
            }
    }

    @Composable
    fun content() =
        FormFactorWrapper() {
            BookDetailScreen(
                state = state,
                readingSettings = readingSettings,
                listState = listState,
                modifier = Modifier.pointerInput(true) {
                    detectTapGestures(
                        onTap = { viewModel.onAction(BookDetailAction.ToggleReadingMode) }
                    )
                }
            )
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
                    readingSettings = readingSettings,
                    onAction = readingControlsViewModel::onAction,
                    onNavBack = onNavigateBack,
                    content = {
                        content()
                    }
                )
            }
        }
    }

}

