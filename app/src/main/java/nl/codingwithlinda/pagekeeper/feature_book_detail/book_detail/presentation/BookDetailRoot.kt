package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettings
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components.BookDetailScreen
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.LocalDefaultTextStyle
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.sliderValueToActualSp
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.typographySliderRange
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailAction
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailState
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.ReadingMode
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingControls
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingControlsViewModel
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingOrientation
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControlAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@Composable
fun BookDetailRoot(
    isbn: String,
    onNavigateBack: () -> Unit,
    viewModel: BookDetailViewModel = koinViewModel(key = isbn) { parametersOf(isbn) },
    readingControlsViewModel: ReadingControlsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val readingSettings by readingControlsViewModel.state.collectAsStateWithLifecycle()

    val activity = LocalActivity.current

    activity?.let{
        it.requestedOrientation = when(readingSettings.orientation){
            ReadingOrientation.AUTO_ROTATE -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            ReadingOrientation.LOCKED_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        }
    }

    val listState = rememberLazyListState()

    var anchorIndex by remember { mutableIntStateOf(0) }
    var anchorRatio by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val itemSize = listState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.index == index }?.size ?: return@collect
                anchorIndex = index
                anchorRatio = if (itemSize > 0) offset.toFloat() / itemSize else 0f
            }
    }

    LaunchedEffect(readingSettings.fontSize) {
        val layout = snapshotFlow { listState.layoutInfo }
            .first { it.visibleItemsInfo.any { item -> item.index == anchorIndex } }
        val itemSize = layout.visibleItemsInfo
            .firstOrNull { it.index == anchorIndex }?.size ?: return@LaunchedEffect
        listState.scrollToItem(anchorIndex, (anchorRatio * itemSize).toInt())
    }

    @Composable
    fun content() =
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
        var indicatorNaturalBounds by remember { mutableStateOf(Rect.Zero) }

        val valueRange = typographySliderRange()

        val sliderState = rememberSliderState(
            value = readingSettings.fontSize,
            valueRange = valueRange,
        )

        LaunchedEffect(readingSettings.fontSize) {
            sliderState.value = readingSettings.fontSize
        }

        Column(modifier = Modifier.padding(innerPadding)) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (showAdjustFontSize) {
                    // onGloballyPositioned before offset → reports natural (pre-offset) bounds.
                    // offset then moves the indicator so its center-x aligns with the thumb
                    // center-x and its bottom sits just above the thumb top.
                    // The 4dp gap is the only intentional design constant here.
                    Box(
                        modifier = Modifier
                            .alpha(if (indicatorNaturalBounds == Rect.Zero) 0f else 1f)
                            .onGloballyPositioned { indicatorNaturalBounds = it.boundsInRoot() }
                            .offset {
                                if (thumbBounds == Rect.Zero || indicatorNaturalBounds == Rect.Zero)
                                    return@offset IntOffset.Zero
                                val dx = thumbBounds.center.x - indicatorNaturalBounds.center.x
                                val dy = thumbBounds.top - indicatorNaturalBounds.bottom - 4.dp.toPx()
                                IntOffset(dx.roundToInt(), dy.roundToInt())
                            }
                            .pointerInput(true){
                                this.detectHorizontalDragGestures(
                                    onHorizontalDrag = { _, dragAmount ->
                                        val rangeSize = valueRange.endInclusive - valueRange.start
                                        val valueDelta = dragAmount / trackBounds.width * rangeSize
                                        val newFontSize = (sliderState.value + valueDelta)
                                            .coerceIn(valueRange.start, valueRange.endInclusive)
                                        sliderState.value = newFontSize
                                    },
                                    onDragEnd = {
                                        onAction(ReadingControlAction.AdjustFontSize(sliderState.value))
                                    }
                                )
                            }
                            .shadow(elevation = 3.dp, shape = CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest, shape = CircleShape)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val baseSp = LocalDefaultTextStyle.current.fontSize.value
                        val actualSp = sliderValueToActualSp(readingSettings.fontSize, baseSp)
                        Text("${actualSp.roundToInt()}")
                    }
                }

                ReadingControls(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    sliderState = sliderState,
                    readingSettings = readingSettings,
                    showAdjustFontSize = showAdjustFontSize,
                    toggleAdjustFontSize = { showAdjustFontSize = true },
                    onAction = onAction,
                    onThumbPositioned = { thumbBounds = it },
                    onTrackPositioned = { trackBounds = it }
                )
            }
        }
    }
}