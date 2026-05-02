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
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components.BookDetailScreen
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components.FontSizeIndicator
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

    LaunchedEffect(state.currentSection) {
        if (!scrollSettled  && state.currentSection >= 0) {
            println("--- BOOK DETAIL --- SCROLLING TO SECTION ${state.currentSection} at position ${state.currentSectionOffset}")
           listState.scrollToItem(
                index = state.currentSection.coerceAtMost(state.pages.size - 1),
                scrollOffset = state.currentSectionOffset
            )
            scrollSettled = true
        }
    }

    val nearTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }
    val nearBottom by remember {
        derivedStateOf {
            val lastIndex  = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: return@derivedStateOf false
            val total = listState.layoutInfo.totalItemsCount
            lastIndex >= total -2
        }
    }

    // Save reading position once the initial scroll has settled
    LaunchedEffect(nearTop, nearBottom) {
        println("--- BOOK DETAIL --- SCROLL SETTLED: $scrollSettled")
        if (!scrollSettled) return@LaunchedEffect
        snapshotFlow {
            val firstItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            val key = firstItem?.key as? String ?: ""
            val sectionId = key.toIntOrNull() ?: -1
            val offset = listState.firstVisibleItemScrollOffset
            sectionId to offset
        }.debounce(500)
            .collect { (sectionId, offset) ->
                println("--- BOOK DETAIL --- firstItem. sectionId = $sectionId, paragraphId = $offset")

                if (sectionId != -1) {
                    val orientation = configuration.orientation
                    viewModel.onAction(BookDetailAction.PlaceBookmark(sectionId, offset, orientation))
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
                onAction = viewModel::onAction,
                modifier = Modifier.pointerInput(true) {
                    detectTapGestures(
                        onTap = { viewModel.onAction(BookDetailAction.ToggleReadingMode) }
                    )
                }
            )
        }


    when(state.readingMode){
        ReadingMode.IMMERSIVE -> {
            content()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScaffold(
    modifier: Modifier = Modifier,
    state: BookDetailState,
    readingSettings: ReadingSettings,
    onAction: (ReadingControlAction) -> Unit,
    onNavBack: () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (state.book == null) return
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(state.book.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavBack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "back_to_library"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onAction(ReadingControlAction.ToggleFavorite(state.book.isbn ))

                    }) {
                        val icon = if (state.book.isFavorite) R.drawable.menu_favorites_active else R.drawable.favorites
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = "mark_favorite"
                        )
                    }
                }
            )
        },

        ) {innerPadding ->

        var showAdjustFontSize by rememberSaveable(state.readingMode) {
            mutableStateOf(false)
        }
        var thumbBounds by remember { mutableStateOf(Rect.Zero) }
        var trackBounds by remember { mutableStateOf(Rect.Zero) }

        val valueRange = typographySliderRange()

        val sliderState = rememberSliderState(
            value = readingSettings.fontSize,
            valueRange = valueRange,
        )

        LaunchedEffect(readingSettings.fontSize) {
            sliderState.value = readingSettings.fontSize
        }

        Column(modifier = Modifier
            .safeContentPadding()
            .consumeWindowInsets(innerPadding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {

                val device = rememberDeviceConfig()
                val listControls = device.deviceType.toReadingControls().map {
                    when(it){
                        ReadingControl.AUTO_ROTATE -> {
                            AutoRotateControl(
                                setting = readingSettings.orientation,
                                onAction = { onAction(ReadingControlAction.ToggleAutoRotate) }
                            )
                        }
                        ReadingControl.FONT_SIZE -> {
                            FontSizeControl(
                                onAction = { showAdjustFontSize = true }
                            )
                        }
                    }
                }
                if (showAdjustFontSize) {
                    FontSizeIndicator(
                        fontSize = readingSettings.fontSize,
                        thumbBounds = thumbBounds,
                        trackBounds = trackBounds,
                        valueRange = valueRange,
                        sliderState = sliderState,
                        onAction = onAction,
                    )
                }

                ReadingControlsAdaptive(
                    collapsed = !showAdjustFontSize,
                    contentCollapsed = {
                        ControlsRow(
                            modifier = Modifier.fillMaxWidth()
                                .padding(vertical = 8.dp),
                            items = listControls
                        )
                    },
                    contentExpanded = {
                        FontSizeSlider(
                            modifier = Modifier,
                            sliderState = sliderState,
                            currentFontSize = readingSettings.fontSize,
                            valueRange = valueRange,
                            onSizeChange = { onAction(ReadingControlAction.AdjustFontSize(it)) },
                            onThumbPositioned = { thumbBounds = it },
                            onTrackPositioned = { trackBounds = it }
                        )
                    }

                )
            }
        }
    }
}